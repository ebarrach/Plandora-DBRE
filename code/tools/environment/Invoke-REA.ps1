[CmdletBinding()]
param(
    [Parameter(Position=0, ValueFromRemainingArguments=$true)]
    [string[]]$ReaArguments
)
$ErrorActionPreference = 'Stop'
$Entry = Join-Path $env:LOCALAPPDATA 'Plandora-DBRE\rea\6.8.0\node_modules\rea-agents\scripts\rea.mjs'
if (-not (Test-Path -LiteralPath $Entry)) {
    throw 'Run Install-REA.ps1 first.'
}
if (-not $ReaArguments -or $ReaArguments.Count -eq 0) { $ReaArguments = @('--help') }
& node $Entry @ReaArguments
if ($LASTEXITCODE -ne 0) { throw "REA exited with code $LASTEXITCODE." }
