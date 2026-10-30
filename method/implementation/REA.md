# REA integration

Official upstream: https://github.com/morluto/rea
Source pin: `b1ff7cdacc03110d51bcac33da3cbb7850ac27fc`.
The pinned source declares package version 6.8.0.

## Locations

- `code/external/rea/`: Git submodule, original sources and documentation.
- `code/tools/environment/Install-REA.ps1`: isolated Windows installation.
- `code/tools/environment/Invoke-REA.ps1`: invoke that installation.
- Runtime: `%LOCALAPPDATA%/Plandora-DBRE/rea/6.8.0/`, outside the target project.
- Evidence from actual complementary analyses: `results/step-2/complementary/rea/`.

## Windows setup

Run from the repository root:

```powershell
git submodule update --init -- code/external/rea
powershell -NoProfile -ExecutionPolicy Bypass -File code/tools/environment/Install-REA.ps1 -ConfigureCodex
powershell -NoProfile -ExecutionPolicy Bypass -File code/tools/environment/Invoke-REA.ps1 doctor --json
```

Node.js requirements from the pinned package:
22.x >=22.23.4, 24.x >=24.22.1, or >=26.12.1.
The installer checks them and stops on incompatibility.
If version 6.8.0 is not published, report it; do not substitute latest.
npm's lockfile remains with the local runtime.

The optional ConfigureCodex switch runs the official setup for Codex only.
Review its configuration plan. Skills are disabled by this script.
Restart the external Codex agent used in Air and check that REA is available.
Direct Air-native MCP integration has not been tested here.

## Appropriate use

REA is complementary tooling. Its published target list does not establish
general Java-source/JDBC extraction or MySQL-schema analysis support.
Do not describe it as a replacement for SQLInspect.
Java/SQL source inspection by an agent remains distinct from REA tool execution.

A missing native analysis engine in doctor is not evidence that Java/JDBC
analysis needs that engine. Install only providers needed for an actual task.
No Ghidra, Hopper, Android tooling or application deployment is configured.

Record each actual command/tool call, version, target revision, evidence paths
and limitations. Distinguish observed facts, candidate relationships and
validated constraints. Never claim that installation or analysis succeeded
merely because this configuration exists.

## Reproduction

Clone with `git clone --recurse-submodules`, or initialize the submodule later.
Do not use `git submodule update --remote` for a reproducible submission.
The source submodule and published npm runtime are separate acquisitions;
a source version label alone does not establish identical package bytes.
