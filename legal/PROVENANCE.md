# Source provenance

Upstream repository:
https://svn.code.sf.net/p/plandora/code/

Reference revision: 348.
Expected location: code/plandora/reference/r348/

Preserve upstream copyright and licence notices.
Retrieval metadata: source-version.json.

## Two distinct checksum representations

- source-acquisition-manifest.csv preserves the original Windows acquisition
  manifest byte for byte. It describes that acquisition, not Git blob bytes.
- source-manifest.csv describes the committed r348 Git blobs. Verify it with:
  python code/tools/validation/reference_manifest.py

The first import used Git text normalization. Reproduced against commit
ef13aae1a769ff1fba1ea4eecc0e32f01cefbab4: 435 acquisition checksums match Git
blobs, 1,089 additional entries match after LF-to-CRLF conversion, and seven
do not match that conversion. Their cause is not established by this check.
The original manifest is retained rather than presenting regenerated checksums
as the original SVN bytes. The Git snapshot is not certified byte-identical
to the acquisition or a fresh SVN export.

The seven residual paths are docs/plandora_export.xml, EVA.jrxml,
KPIHistogram.jrxml, projectReqAttendTime.jrxml, projectReqPriority.jrxml,
projectReqRequesters.jrxml and projectReqStatus.jrxml.

Reference paths now use -text in .gitattributes to preserve the currently
committed representation in future checkouts. No reference blob was changed
by this manifest repair. Verification hashes Git blobs, independently of
core.autocrlf and working-tree edits. Use git status separately to detect
uncommitted working-tree changes. Regeneration (--write) is an intentional
maintenance action, not a substitute for investigation of a changed reference.
Third-party sources are not relicensed by this repository.
