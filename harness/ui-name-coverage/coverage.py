"""FoodUs source-call inventory; lexical candidate coverage, never a Kotlin compiler substitute."""
from __future__ import annotations

import argparse
import datetime
import json
from pathlib import Path
import re
import sys

MODULE = Path(__file__).resolve().parent
ROOT = MODULE.parent.parent


def mask(source: str) -> str:
    """Keep code offsets while hiding nested comments and string/character literals."""
    out = list(source)
    index = 0
    while index < len(source):
        start = index
        literal = False
        if source.startswith('//', index):
            index = source.find('\n', index)
            if index < 0:
                index = len(source)
        elif source.startswith('/*', index):
            index += 2
            depth = 1
            while index < len(source) and depth:
                if source.startswith('/*', index):
                    depth += 1
                    index += 2
                elif source.startswith('*/', index):
                    depth -= 1
                    index += 2
                else:
                    index += 1
        elif source.startswith('"""', index):
            literal = True
            end = source.find('"""', index + 3)
            index = len(source) if end < 0 else end + 3
        elif source[index] in ('"', "'"):
            literal = True
            quote = source[index]
            index += 1
            while index < len(source):
                if source[index] == '\\':
                    index += 2
                elif source[index] == quote:
                    index += 1
                    break
                else:
                    index += 1
        else:
            index += 1
            continue
        for pos in range(start, min(index, len(source))):
            if out[pos] not in '\r\n':
                out[pos] = ' '
        if literal:
            # Retain token boundaries so positional literals remain arguments and edits cannot
            # accidentally be inserted before a final string/character token.
            out[start] = 'X'
            out[min(index, len(source)) - 1] = 'X'
    return ''.join(out)


def matching(code: str, start: int) -> int:
    opens = {'(': ')', '[': ']', '{': '}'}
    stack = [opens[code[start]]]
    for pos in range(start + 1, len(code)):
        char = code[pos]
        if char in opens:
            stack.append(opens[char])
        elif char in ')]}':
            if not stack or char != stack.pop():
                raise ValueError(f'Unbalanced source at offset {pos}')
            if not stack:
                return pos
    raise ValueError(f'Unclosed source at offset {start}')


def arguments(code: str, opening: int, closing: int) -> list[tuple[int, int]]:
    result = []
    start = opening + 1
    pos = start
    while pos < closing:
        if code[pos] in '([{':
            pos = matching(code, pos) + 1
        elif code[pos] == ',':
            if code[start:pos].strip():
                result.append((start, pos))
            start = pos + 1
            pos += 1
        else:
            pos += 1
    if code[start:closing].strip():
        result.append((start, closing))
    return result


def functions(source: str, path: str) -> list[dict]:
    code = mask(source)
    package = re.search(r'^package\s+([\w.]+)', code, re.M)
    if not package:
        return []
    result = []
    classes = []
    for declaration in re.finditer(r'\b(?:class|object)\s+(\w+)', code):
        cursor = declaration.end()
        while cursor < len(code):
            if code[cursor] in '([':
                cursor = matching(code, cursor) + 1
            elif code[cursor] == '{':
                classes.append((declaration[1], cursor, matching(code, cursor)))
                break
            elif code[cursor] in ';=' or re.match(r'\b(?:fun|val|var|class|object)\b', code[cursor:]):
                break
            else:
                cursor += 1
    pattern = r'\bfun\s+(?:<[^()\n]+?>\s*)?(?:[\w?.]+(?:<[^()\n]+>)?\.)?(\w+)\s*\('
    for match in re.finditer(pattern, code):
        opening = match.end() - 1
        closing = matching(code, opening)
        params = arguments(code, opening, closing)
        modifier = next((i for i, (a, b) in enumerate(params) if re.match(r'\s*modifier\s*:', code[a:b])), None)
        parameter_names = [parameter_match[1] for a, b in params
                           if (parameter_match := re.match(r'\s*(?:(?:crossinline|noinline|vararg)\s+)?(\w+)\s*:', code[a:b]))]
        body = closing + 1
        while body < len(code) and code[body].isspace():
            body += 1
        end = matching(code, body) if body < len(code) and code[body] == '{' else closing
        preceding = code[max(0, match.start() - 150):match.start()]
        line_start = code.rfind('\n', 0, match.start()) + 1
        private = bool(re.search(r'\bprivate\b', code[line_start:match.start()]))
        containing = [name for name, start, end in classes if start < match.start() < end]
        owner = '.'.join([package[1]] + containing + [match[1]])
        result.append(dict(name=match[1], symbol=owner, path=path,
                           start=match.start(), nameStart=match.start(1), body=body, end=end,
                           signatureOpening=opening, parameters=parameter_names,
                           modifier=modifier, composable='@Composable' in preceding, private=private))
    return result


def inventory(source: str, path: str, catalog: dict[str, list[dict]], config: dict) -> list[dict]:
    code = mask(source)
    package = re.search(r'^package\s+([\w.]+)', code, re.M)[1]
    imports = re.findall(r'^import\s+([\w.*]+)(?:\s+as\s+(\w+))?', code, re.M)
    explicit_candidates = {}
    for symbol, alias in imports:
        if not symbol.endswith('.*'):
            explicit_candidates.setdefault(alias or symbol.rsplit('.', 1)[-1], []).append(symbol)
    explicit = {name: symbols[0] for name, symbols in explicit_candidates.items()}
    wildcards = {symbol[:-2] for symbol, _ in imports if symbol.endswith('.*')}
    definitions = functions(source, path)
    local_types = set(re.findall(r'\b(?:class|object|interface)\s+(\w+)', code))
    declarations = {entry['nameStart'] for entry in definitions}
    framework = {name: prefix + '.' + name for prefix, names in config['framework'].items() for name in names}
    surfaces = {name: prefix + '.' + name for prefix, names in config['surfaces'].items() for name in names}
    framework_symbols = set(framework.values())
    surface_symbols = set(surfaces.values())
    scopes = []
    for scope in re.finditer(r'\bUiSourceScope\s*\(', code):
        scope_open = scope.end() - 1
        scope_close = matching(code, scope_open)
        body = scope_close + 1
        while body < len(code) and code[body].isspace():
            body += 1
        if body < len(code) and code[body] == '{':
            scopes.append((body, matching(code, body), source[scope_open:scope_close]))
    output = []
    for match in re.finditer(r'(?<![\w.])((?:[A-Za-z_]\w*\.)*[A-Z]\w*)\s*(?:<[^{};]*?>\s*)?([({])', code):
        callee = match[1]
        name = callee.rsplit('.', 1)[-1]
        if name in local_types:
            continue
        if any(it['start'] <= match.start(1) <= it['signatureOpening'] for it in definitions):
            continue
        if re.search(r'\b(?:class|object|interface)\s*$', code[max(0, match.start() - 50):match.start()]):
            continue
        opening = match.end() - 1
        closing = matching(code, opening)
        args = arguments(code, opening, closing) if code[opening] == '(' else []
        named = {parameter[1] for a, b in args
                 if (parameter := re.match(r'\s*(\w+)\s*=', code[a:b]))}
        expected = surfaces.get(name) or framework.get(name)
        all_symbols = framework_symbols | surface_symbols | {it['symbol'] for values in catalog.values() for it in values}
        symbol = callee if callee in all_symbols else explicit.get(name)
        if '.' in callee and callee.split('.')[0] in explicit:
            first, rest = callee.split('.', 1)
            symbol = explicit[first] + '.' + rest
        if symbol is None and expected and (expected.rsplit('.', 1)[0] in wildcards):
            symbol = expected
        possible = [it for it in catalog.get(name, []) if it['path'] == path or not it.get('private', False)]
        local = next((it for it in possible if it['path'] == path and named.issubset(it['parameters'])), None)
        if local is not None:
            symbol = local['symbol']
        elif symbol not in framework_symbols and expected in explicit_candidates.get(name, []):
            project = next((it for it in possible if it['symbol'] == symbol), None)
            if project is None or not named.issubset(project['parameters']):
                symbol = expected
        if symbol is None:
            candidate = next((it for it in possible if it['symbol'].rsplit('.', 1)[0] == package), None)
            candidate = candidate or next((it for it in possible if it['symbol'].rsplit('.', 1)[0] in wildcards), None)
            symbol = candidate['symbol'] if candidate else None
        known = next((it for it in possible if it['symbol'] == symbol), None)
        surface = symbol is not None and symbol in surface_symbols
        scope_only = known is not None and (name.endswith('Screen') or name in config.get('scopeFunctions', []))
        if not surface and not scope_only and symbol not in framework_symbols and not (known and known['modifier'] is not None):
            continue
        owner = next((it['symbol'] for it in reversed(definitions) if it['body'] <= match.start() <= it['end']), None)
        if owner is None:
            # Composable expression bodies are handled by the preceding declaration.
            owner = next((it['symbol'] for it in reversed(definitions) if it['start'] < match.start()), None)
        if owner is None:
            continue
        modifier = next(((a, b) for a, b in args if re.match(r'\s*modifier\s*=', code[a:b])), None)
        if modifier is None:
            modifier = next(((a, b) for a, b in args if re.match(r'\s*\(*\s*(?:Modifier|modifier)\b', code[a:b])), None)
        modifier_code = code[modifier[0]:modifier[1]] if modifier else ''
        chain_code = list(modifier_code)
        cursor = 0
        while cursor < len(modifier_code):
            if modifier_code[cursor] == '{':
                end = matching(modifier_code, cursor)
                chain_code[cursor:end + 1] = ' ' * (end + 1 - cursor)
                cursor = end + 1
            else:
                cursor += 1
        marked = bool(re.search(r'\.uiName\s*\(', ''.join(chain_code)))
        # A surface scope immediately surrounding the call preserves cross-window source ancestry.
        if surface or scope_only:
            scope_marked = any(start < match.start() < end and json.dumps(symbol) in metadata
                               for start, end, metadata in scopes)
            if surface and name in ('Dialog', 'Popup'):
                content_start = closing + 1
                while content_start < len(code) and code[content_start].isspace():
                    content_start += 1
                marked = False
                if content_start < len(code) and code[content_start] == '{':
                    content_end = matching(code, content_start)
                    for wrapper in re.finditer(r'\bUiInspectionSurface\s*\(', code[content_start:content_end]):
                        wrapper_open = content_start + wrapper.end() - 1
                        wrapper_close = matching(code, wrapper_open)
                        if json.dumps(symbol) in source[wrapper_open:wrapper_close]:
                            marked = True
            marked = scope_marked and (marked if surface else True)
        output.append(dict(name=name, symbol=symbol, path=path, owner=owner,
                           start=match.start(), opening=opening, closing=closing, args=args,
                           modifier=modifier, marked=marked, surface=surface,
                           scopeOnly=scope_only,
                           line=source.count('\n', 0, match.start()) + 1))
    return output


def survey(root: Path, config: dict) -> tuple[list[dict], dict[str, str], dict[str, list[dict]]]:
    sources = {}
    for folder in config['sourceRoots']:
        source_root = root / folder
        if not source_root.is_dir():
            raise ValueError(f'Configured source root does not exist: {folder}')
        files = list(source_root.rglob('*.kt'))
        if not files:
            raise ValueError(f'Configured source root has no Kotlin files: {folder}')
        for file in files:
            path = file.relative_to(root).as_posix()
            if not any(excluded in '/' + path for excluded in config['excludedPaths']):
                sources[path] = file.read_text(encoding='utf-8-sig')
    catalog = {}
    for path, source in sources.items():
        for function in functions(source, path):
            if function['composable']:
                catalog.setdefault(function['name'], []).append(function)
    calls = [call for path, source in sources.items() for call in inventory(source, path, catalog, config)]
    if not calls:
        raise ValueError('Source survey found no configured Compose calls')
    return calls, sources, catalog


def literal_names(source: str, path: str) -> list[dict]:
    code = mask(source)
    declarations = []
    for method in re.finditer(r'(?:\.uiName|\buiInspectionSource\.element)\s*\(', code):
        opening = method.end() - 1
        literal = re.match(r'\s*"([A-Za-z_][A-Za-z0-9_.-]*)"\s*,\s*"([^"]+)"', source[opening + 1:])
        if literal:
            declarations.append(dict(name=literal[1], symbol=literal[2], path=path,
                                     start=opening + 1 + literal.start(1), end=opening + 1 + literal.end(1)))
    return declarations


def self_test(config: dict) -> None:
    source = '''package com.example
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
@Composable fun Screen() {
    // Text("not code")
    val ignored = "Text(fake)"
    Column(modifier = Modifier.uiName("parent", "a.b", "c.d", "app/F.kt")) {
        Text("named", modifier = Modifier.uiName("child", "a.b", "c.d", "app/F.kt"))
        Text("deliberately unnamed")
    }
}
'''
    calls = inventory(source, 'app/F.kt', {}, config)
    assert len(calls) == 3
    assert [it['marked'] for it in calls] == [True, True, False]
    assert calls[-1]['name'] == 'Text'
    assert len(inventory(source.replace('Text("deliberately unnamed")', ''), 'app/F.kt', {}, config)) == 2
    tooltip = 'package com.example\nimport androidx.compose.material3.PlainTooltip\n@Composable fun Screen() { PlainTooltip {} }'
    assert not inventory(tooltip, 'app/F.kt', {}, config)[0]['marked']
    assert inventory(tooltip.replace('PlainTooltip {}', 'PlainTooltip(modifier=Modifier.uiName("tooltip.root")) {}'),
                     'app/F.kt', {}, config)[0]['marked']
    title = 'package com.example\nimport androidx.compose.material3.DatePickerDefaults\n@Composable fun Screen() { DatePickerDefaults.DatePickerTitle(displayMode=state.displayMode) }'
    assert not inventory(title, 'app/F.kt', {}, config)[0]['marked']
    assert mask('/* outer /* nested */ done */ Text("a")').strip() == 'Text(X X)'
    prefix = 'package com.example\nimport androidx.compose.ui.window.Dialog\n@Composable fun Screen() {\n'
    for invalid in (
        '// UiSourceScope(UiSourceDescriptor("dialog", "androidx.compose.ui.window.Dialog")) {\nDialog({}) {}',
        'UiSourceScope(UiSourceDescriptor("dialog", "androidx.compose.ui.window.Dialog")) {}\nDialog({}) {}',
    ):
        assert not inventory(prefix + invalid + '\n}', 'app/F.kt', {}, config)[0]['marked']
    valid = ('UiSourceScope(UiSourceDescriptor("dialog", "androidx.compose.ui.window.Dialog")) { Dialog({}) { '
             'UiInspectionSurface(UiSourceDescriptor("dialog.content", "androidx.compose.ui.window.Dialog")) {} } }')
    assert inventory(prefix + valid + '\n}', 'app/F.kt', {}, config)[0]['marked']
    assert inventory('package com.example\n@Composable fun Screen() { SomeState() }', 'app/F.kt', {}, config) == []
    alias = 'package com.example\nimport androidx.compose.material3.Text as Label\n@Composable fun Screen() { Label("missing") }'
    assert not inventory(alias, 'app/F.kt', {}, config)[0]['marked']
    qualified = 'package com.example\n@Composable fun Screen() { androidx.compose.material3.Text("missing") }'
    assert not inventory(qualified, 'app/F.kt', {}, config)[0]['marked']
    generic = 'package com.example\nimport androidx.compose.animation.AnimatedContent\n@Composable fun Screen() { AnimatedContent<Int>(targetState = 1) {} }'
    assert not inventory(generic, 'app/F.kt', {}, config)[0]['marked']
    nested = '''package com.example
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
@Composable fun Screen() { Column(modifier = Modifier.composed {
    Text("nested", modifier = Modifier.uiName("child")); Modifier
}) {} }
'''
    assert not inventory(nested, 'app/F.kt', {}, config)[0]['marked']
    try:
        survey(ROOT, dict(config, sourceRoots=['does-not-exist']))
        raise AssertionError('Missing source root must fail')
    except ValueError:
        pass
    receiver = '''package com.example
import androidx.compose.material3.Icon
import com.example.Icon
import androidx.compose.material3.OutlinedTextField
@Composable fun SponsorMethod.Icon(modifier: Modifier = Modifier) {
    Icon(imageVector = vector, contentDescription = null)
}
@Composable fun Field<Double, Error>.TextField(modifier: Modifier = Modifier) {
    OutlinedTextField(state = state)
}
@Composable fun Screen() { state.protein.TextField(); item.Icon(Modifier) }
'''
    functions_found = functions(receiver, 'app/F.kt')
    catalog = {}
    for function in functions_found:
        catalog.setdefault(function['name'], []).append(function)
    calls = inventory(receiver, 'app/F.kt', catalog, config)
    assert len(calls) == 4, calls  # Neither extension declaration is a constructor call.
    assert calls[0]['symbol'] == 'androidx.compose.material3.Icon'
    assert calls[1]['owner'] == 'com.example.TextField'
    assert calls[2]['symbol'] == 'com.example.TextField'
    assert calls[3]['symbol'] == 'com.example.Icon'
    assert not inventory('package com.example\nimport androidx.compose.material3.Text\n'
                         '@Composable fun Screen() { Text("literal") }', 'app/F.kt', {}, config)[0]['marked']
    assert inventory('package com.example\n@Composable fun Screen() {}\nobject Screen {}',
                     'app/F.kt', {'Screen': [dict(symbol='com.example.Screen', path='app/F.kt', modifier=None,
                                               parameters=[])]}, config) == []
    private_catalog = {'Goal': [dict(symbol='com.example.Goal', path='Other.kt', modifier=0,
                                     parameters=['modifier'], private=True)]}
    model = 'package com.example\nprivate class Goal(val value: Int)\nfun model() = Goal(1)'
    assert inventory(model, 'Model.kt', private_catalog, config) == []
    assert len(literal_names('Modifier.uiName("same", "a.b", source, "Screen")\n'
                             'Modifier.uiName("same", "a.b", source, "Screen")', 'app/F.kt')) == 2


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument('--self-test', action='store_true')
    args = parser.parse_args()
    report = {}
    try:
        config = json.loads((MODULE / 'config.json').read_text())
        if args.self_test:
            self_test(config)
            report = dict(pass_=True, selfTest=True, reason='Deliberately unnamed call is detected; comments/strings excluded')
        else:
            calls, sources, _ = survey(ROOT, config)
            missing = [{key: call[key] for key in ('path', 'line', 'name', 'owner')} for call in calls if not call['marked']]
            names = {}
            for path, source in sources.items():
                for declaration in literal_names(source, path):
                    names.setdefault(declaration['name'], []).append(declaration)
            duplicates = {name: entries for name, entries in names.items() if len(entries) > 1}
            report = dict(pass_=not missing and not duplicates, files=len(sources), surveyedCalls=len(calls), missing=missing,
                          duplicateNames=duplicates, reason=f'{len(missing)} unnamed surveyed call sites; {len(duplicates)} duplicate source identifiers',
                          limitation='Lexical source survey; runtime and new API review remain required')
        code = 0 if report['pass_'] else 1
    except (ValueError, KeyError, OSError, AssertionError) as error:
        code = 2
        report = dict(pass_=False, reason=f'{type(error).__name__}: {error}')
    stamp = datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%S%fZ')
    destination = MODULE / 'runs' / stamp
    destination.mkdir(parents=True)
    (destination / 'report.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
    print(('PASS' if code == 0 else 'FAIL') + ': ' + report['reason'])
    print('Report: ' + str(destination / 'report.json'))
    return code


if __name__ == '__main__':
    sys.exit(main())
