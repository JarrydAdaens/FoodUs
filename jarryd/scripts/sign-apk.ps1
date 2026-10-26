<#
.SYNOPSIS
Aligns and signs a FoodYou release APK with zipalign + apksigner, then verifies the signature.

.DESCRIPTION
Wraps the same post-build signing pattern as .github/workflows/release-apk.yml so a
distributable APK can be produced locally without CI.

Secret handling (laws.md section 2): the keystore path, key alias, and passwords are read
ONLY from environment variables or interactive prompts — never from script parameters, so
they cannot land in shell history. Nothing secret is echoed or written to disk.

Environment variables (all optional; prompted for when missing and the session is interactive):
  FOODUS_KEYSTORE           Absolute path to the release keystore (.jks). Must be OUTSIDE the repo.
  FOODUS_KEY_ALIAS          Key alias inside the keystore.
  FOODUS_KEYSTORE_PASSWORD  Keystore password.
  FOODUS_KEY_PASSWORD       Key password, only if it differs from the keystore password.

.PARAMETER InputApk
Path to the unsigned APK (default: app\build\outputs\apk\release\foodus-<version>-unsigned.apk,
with the catalog version's dots replaced by underscores).

.PARAMETER OutputApk
Path for the signed APK (default: beside the input, removing its -unsigned suffix;
an input without that suffix gets -signed appended).

.EXAMPLE
$env:FOODUS_KEYSTORE = 'C:\secure\foodus.jks'
$env:FOODUS_KEY_ALIAS = 'foodus'
$env:FOODUS_KEYSTORE_PASSWORD = '<prompted or from password manager>'
.\jarryd\scripts\sign-apk.ps1
#>
[CmdletBinding()]
param(
    [string]$InputApk,
    [string]$OutputApk
)

$ErrorActionPreference = 'Stop'

$sdkRoot = if ($env:ANDROID_HOME) {
    $env:ANDROID_HOME
} elseif ($env:ANDROID_SDK_ROOT) {
    $env:ANDROID_SDK_ROOT
} else {
    Join-Path $env:LOCALAPPDATA 'Android\Sdk'
}
$buildTools = Join-Path $sdkRoot 'build-tools\36.0.0'
$zipalign = Join-Path $buildTools 'zipalign.exe'
$apksigner = Join-Path $buildTools 'apksigner.bat'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

foreach ($tool in @($zipalign, $apksigner)) {
    if (-not (Test-Path $tool)) { throw "Required build tool not found: $tool" }
}

if (-not $InputApk) {
    $catalogPath = Join-Path $repoRoot 'gradle\libs.versions.toml'
    $versionMatches = @(Select-String -LiteralPath $catalogPath -Pattern '^fork-version-name = "([0-9]+(?:\.[0-9]+){3})"$')
    if ($versionMatches.Count -ne 1) {
        throw 'Expected exactly one canonical four-part fork-version-name in the version catalog.'
    }
    $fileVersion = $versionMatches[0].Matches[0].Groups[1].Value.Replace('.', '_')
    $InputApk = Join-Path $repoRoot "app\build\outputs\apk\release\foodus-$fileVersion-unsigned.apk"
}
if (-not (Test-Path $InputApk)) {
    throw "Input APK not found: $InputApk (run .\gradlew.bat :app:assembleRelease first)"
}
if (-not $OutputApk) {
    $inputName = [System.IO.Path]::GetFileNameWithoutExtension($InputApk)
    $signedName = if ($inputName.EndsWith('-unsigned')) {
        $inputName.Substring(0, $inputName.Length - '-unsigned'.Length)
    } else {
        "$inputName-signed"
    }
    $OutputApk = Join-Path (Split-Path $InputApk -Parent) "$signedName.apk"
}

function Get-RequiredValue([string]$EnvName, [string]$PromptText, [switch]$Secret) {
    $value = [Environment]::GetEnvironmentVariable($EnvName)
    if (-not [string]::IsNullOrWhiteSpace($value)) { return $value }
    if (-not [Environment]::UserInteractive -or [Console]::IsInputRedirected) {
        throw "$EnvName is not set and no interactive prompt is available."
    }
    if ($Secret) {
        $secure = Read-Host -Prompt $PromptText -AsSecureString
        return [System.Net.NetworkCredential]::new('', $secure).Password
    }
    return Read-Host -Prompt $PromptText
}

$keystorePath = Get-RequiredValue 'FOODUS_KEYSTORE' 'Keystore path (.jks)'
$keyAlias = Get-RequiredValue 'FOODUS_KEY_ALIAS' 'Key alias'
$keystorePassword = Get-RequiredValue 'FOODUS_KEYSTORE_PASSWORD' 'Keystore password' -Secret

if (-not (Test-Path $keystorePath)) { throw "Keystore not found: $keystorePath" }

# Safety: the keystore must never live inside the repository (laws.md section 2).
$resolvedKeystore = (Resolve-Path $keystorePath).Path
if ($resolvedKeystore.StartsWith($repoRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing to sign: keystore is inside the repository ($repoRoot). Move it outside the repo."
}

$alignedApk = Join-Path ([System.IO.Path]::GetTempPath()) "foodus-aligned-$PID.apk"
try {
    Write-Host "Aligning $InputApk ..."
    & $zipalign -f -p 4 $InputApk $alignedApk
    if ($LASTEXITCODE -ne 0) { throw "zipalign failed (exit $LASTEXITCODE)." }

    Write-Host "Signing -> $OutputApk ..."
    $signArgs = @(
        'sign', '--alignment-preserved',
        '--ks', $resolvedKeystore,
        '--ks-key-alias', $keyAlias,
        '--ks-pass', "pass:$keystorePassword"
    )
    $keyPassword = [Environment]::GetEnvironmentVariable('FOODUS_KEY_PASSWORD')
    if (-not [string]::IsNullOrWhiteSpace($keyPassword)) {
        $signArgs += @('--key-pass', "pass:$keyPassword")
    }
    $signArgs += @('--out', $OutputApk, $alignedApk)
    & $apksigner @signArgs
    if ($LASTEXITCODE -ne 0) { throw "apksigner sign failed (exit $LASTEXITCODE)." }

    Write-Host 'Verifying signature ...'
    & $apksigner verify --print-certs $OutputApk
    if ($LASTEXITCODE -ne 0) { throw "apksigner verify failed (exit $LASTEXITCODE)." }

    Write-Host "Signed APK ready: $OutputApk"
}
finally {
    Remove-Item -Force -ErrorAction SilentlyContinue $alignedApk
}
