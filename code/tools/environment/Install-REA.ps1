[CmdletBinding()]
param([switch]$ConfigureCodex)

$ErrorActionPreference = 'Stop'
$ReaVersion = '6.8.0'
$Node = (Get-Command node -ErrorAction Stop).Source
$Npm = (Get-Command npm.cmd -ErrorAction Stop).Source
$RawVersion = & $Node --version
if ($LASTEXITCODE -ne 0) { throw 'Cannot read Node.js version.' }
$NodeVersion = [version]($RawVersion.Trim() -replace '^v', '' -replace '-.*$', '')
$Supported = (
    ($NodeVersion.Major -eq 22 -and $NodeVersion -ge [version]'22.23.4') -or
    ($NodeVersion.Major -eq 24 -and $NodeVersion -ge [version]'24.22.1') -or
    ($NodeVersion -ge [version]'26.12.1')
)
if (-not $Supported) {
    throw "Unsupported Node.js $RawVersion. Required: 22.23.4+ (22.x), 24.22.1+ (24.x), or 26.12.1+."
}
if (-not $env:LOCALAPPDATA) { throw 'LOCALAPPDATA is unavailable.' }
$Runtime = Join-Path $env:LOCALAPPDATA "Plandora-DBRE\rea\$ReaVersion"
New-Item -ItemType Directory -Force -Path $Runtime | Out-Null

# Acquire the package outside the studied project.
Push-Location $Runtime
try {
    $RegistryRepository = & $Npm view "rea-agents@$ReaVersion" repository.url --registry=https://registry.npmjs.org/
    if ($LASTEXITCODE -ne 0) {
        throw "REA $ReaVersion is unavailable in the registry. Do not silently substitute another version."
    }
    if ($RegistryRepository.Trim() -ne 'git+https://github.com/morluto/rea.git') {
        throw 'Unexpected REA package repository.'
    }
    $PackageFile = Join-Path $Runtime 'package.json'
    if (-not (Test-Path -LiteralPath $PackageFile)) {
        $Package = '{"name":"plandora-rea-runtime","version":"1.0.0","private":true}'
        [IO.File]::WriteAllText($PackageFile, $Package, [Text.UTF8Encoding]::new($false))
    }
    & $Npm install --save-exact "rea-agents@$ReaVersion" --omit=optional --no-audit --no-fund --registry=https://registry.npmjs.org/
    if ($LASTEXITCODE -ne 0) { throw 'REA package installation failed.' }
    $Entry = Join-Path $Runtime 'node_modules\rea-agents\scripts\rea.mjs'
    if (-not (Test-Path -LiteralPath $Entry)) { throw 'REA entry point is missing.' }
    & $Node $Entry --help
    if ($LASTEXITCODE -ne 0) { throw 'REA help check failed.' }

    $Installed = Get-Content (Join-Path $Runtime 'node_modules\rea-agents\package.json') -Raw | ConvertFrom-Json
    if ($Installed.version -ne $ReaVersion) { throw 'Unexpected installed REA version.' }

    if ($ConfigureCodex) {
        # Register only Codex; do not change other clients or install skills.
        & $Node $Entry setup --client codex --skill=false
        if ($LASTEXITCODE -ne 0) { throw 'REA Codex setup failed.' }
    }
    Write-Host "REA package installed: $ReaVersion"
    Write-Host "Entry point: $Entry"
    Write-Host 'Restart the Codex session after completing its MCP setup.'
} finally {
    Pop-Location
}
