param([switch]$SelfTest)
$ErrorActionPreference = 'Stop'
$pythonCommand = Get-Command python -ErrorAction SilentlyContinue
if (-not $pythonCommand) {
    Write-Output 'FAIL: Python 3 is required for UI source inventory.'
    exit 3
}
$arguments = @((Join-Path $PSScriptRoot 'coverage.py'))
if ($SelfTest) { $arguments += '--self-test' }
& $pythonCommand.Source @arguments
exit $LASTEXITCODE
