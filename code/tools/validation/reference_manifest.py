"""Generate or verify SHA-256 checksums of committed reference blobs (Python 3)."""

import argparse
import csv
import hashlib
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[3]
PREFIX = "code/plandora/reference/r348/"


def snapshot(revision):
    entries = subprocess.check_output(
        ["git", "ls-tree", "-rz", "--full-tree", revision, "--", PREFIX], cwd=ROOT
    ).split(b"\0")
    result = {}
    with subprocess.Popen(
        ["git", "cat-file", "--batch"], cwd=ROOT,
        stdin=subprocess.PIPE, stdout=subprocess.PIPE,
    ) as process:
        try:
            for entry in entries:
                if not entry:
                    continue
                metadata, raw_path = entry.split(b"\t", 1)
                mode, kind, sha = metadata.split()
                if kind != b"blob" or mode not in (b"100644", b"100755"):
                    raise ValueError("Unsupported reference entry: " + repr(raw_path))
                process.stdin.write(sha + b"\n")
                process.stdin.flush()
                header = process.stdout.readline().split()
                if len(header) != 3 or header[1] != b"blob":
                    raise ValueError("Unexpected git cat-file response")
                remaining = int(header[2])
                digest = hashlib.sha256()
                while remaining:
                    chunk = process.stdout.read(min(remaining, 1024 * 1024))
                    if not chunk:
                        raise ValueError("Truncated Git blob")
                    digest.update(chunk)
                    remaining -= len(chunk)
                if process.stdout.read(1) != b"\n":
                    raise ValueError("Invalid Git blob separator")
                path = raw_path.decode("utf-8")
                result[path[len(PREFIX):]] = digest.hexdigest().upper()
        finally:
            process.stdin.close()
        if process.wait() != 0:
            raise ValueError("git cat-file failed")
    if not result:
        raise ValueError("No reference files found")
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--revision", default="HEAD")
    parser.add_argument("--manifest", type=Path, default=ROOT / "legal/source-manifest.csv")
    parser.add_argument("--write", action="store_true")
    args = parser.parse_args()
    actual = snapshot(args.revision)
    if args.write:
        args.manifest.parent.mkdir(parents=True, exist_ok=True)
        with args.manifest.open("w", encoding="utf-8", newline="") as stream:
            writer = csv.writer(stream, lineterminator="\n")
            writer.writerow(["path", "sha256"])
            writer.writerows(sorted(actual.items()))
        print(f"Written {len(actual)} committed-blob checksums.")
        return 0
    with args.manifest.open(encoding="utf-8-sig", newline="") as stream:
        rows = list(csv.DictReader(stream))
    expected = {row["path"]: row["sha256"].upper() for row in rows}
    if len(expected) != len(rows):
        raise ValueError("Duplicate manifest paths")
    missing = sorted(expected.keys() - actual.keys())
    extra = sorted(actual.keys() - expected.keys())
    changed = sorted(path for path in actual.keys() & expected.keys()
                     if actual[path] != expected[path])
    print(f"Files: {len(actual)}; missing: {len(missing)}; extra: {len(extra)}; changed: {len(changed)}")
    for label, paths in (("missing", missing), ("extra", extra), ("changed", changed)):
        for path in paths[:10]:
            print(f"{label}: {path}")
    return 1 if missing or extra or changed else 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError, KeyError, subprocess.SubprocessError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        sys.exit(2)
