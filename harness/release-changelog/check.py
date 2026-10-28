"""Check the in-app highlights for a release candidate; no publication side effects."""

import argparse
from datetime import date, datetime, timezone
import json
from pathlib import Path
import re
import sys


def configuration():
    settings = json.loads(Path(__file__).with_name("config.json").read_text(encoding="utf-8"))
    for key in ("catalog", "source"):
        value = settings.get(key)
        if not isinstance(value, str) or not value or Path(value).is_absolute() or ".." in Path(value).parts:
            raise ValueError(f"Configuration {key} must be a relative project path")
    pattern = settings.get("entry_pattern")
    if not isinstance(pattern, str) or re.compile(pattern).groups != 1:
        raise ValueError("Configuration entry_pattern must capture one version")
    return settings


def check(catalog, source, entry_pattern):
    version = re.search(r'(?m)^fork-version-name\s*=\s*"([^"\n]+)"', catalog)
    if version is None:
        raise ValueError("Missing catalog fork-version-name")
    expected = version[1]
    entries = list(re.finditer(entry_pattern, source))
    names = [entry[1] for entry in entries]
    issues = []
    if not names or names[0] != expected:
        issues.append("Newest in-app entry must match the catalog version")
    if len(set(names)) != len(names):
        issues.append("Duplicate in-app version entry")
    if expected in names:
        index = names.index(expected)
        end = entries[index + 1].start() if index + 1 < len(entries) else len(source)
        candidate = source[entries[index].start():end]
        stamp = re.search(r'date\s*=\s*LocalDate\(\s*(\d+),\s*(\d+),\s*(\d+)\s*\)', candidate)
        try:
            if stamp is None:
                raise ValueError()
            date(*(int(part) for part in stamp.groups()))
        except ValueError:
            issues.append("Candidate needs a valid release date")
        if not re.search(r'(?:newFeatures|changes|bugFixes)\s*=\s*listOf\(\s*"[^"\s]', candidate):
            issues.append("Candidate needs user-facing highlights")
    return {"version": expected, "entries": names, "issues": issues, "passed": not issues}


def self_test(entry_pattern):
    catalog = 'fork-version-name = "3.2.41.5"'
    def entry(version="3.2.41.5", stamp="2026, 10, 28", highlights='changes = listOf("Add food more easily.")'):
        return f'    Version(\n        version = "{version}",\n        date = LocalDate({stamp}),\n        {highlights},\n    ),\n'
    cases = [
        (entry(), True),
        ("", False),
        (entry("3.2.39.4"), False),
        (entry() + entry(), False),
        (entry("3.2.39.4") + entry(), False),
        (entry(highlights="changes = emptyList()"), False),
        (entry(highlights='changes = listOf("")'), False),
        (entry(stamp="2026, 2, 30"), False),
    ]
    for index, (source, expected) in enumerate(cases):
        if check(catalog, source, entry_pattern)["passed"] != expected:
            raise AssertionError(f"Fixture {index} failed")
    print(f"PASS: {len(cases)} release-changelog fixtures")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    try:
        settings = configuration()
        if args.self_test:
            self_test(settings["entry_pattern"])
            return 0
        result = check(
            (args.root / settings["catalog"]).read_text(encoding="utf-8"),
            (args.root / settings["source"]).read_text(encoding="utf-8"),
            settings["entry_pattern"],
        )
        output = Path(__file__).resolve().parent / "runs" / datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%S%fZ")
        output.mkdir(parents=True)
        (output / "report.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
        print(f"{'PASS' if result['passed'] else 'FAIL'}: in-app highlights for {result['version']}"
              + (": " + "; ".join(result["issues"]) if result["issues"] else ""))
        print(f"Report: {output / 'report.json'}")
        return 0 if result["passed"] else 1
    except (OSError, ValueError, re.error, AssertionError) as error:
        print(f"FAIL: {error}")
        return 2


if __name__ == "__main__":
    sys.exit(main())
