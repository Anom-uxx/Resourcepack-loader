#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  scripts/release.sh bugfix [--no-build]
  scripts/release.sh feature [--no-build]

Versioning policy:
  - bugfix  => +0.0.1 (patch)
  - feature => +1.0.0 (major), resets minor/patch to 0

Defaults:
  - Runs gradle build after bump (disable with --no-build)

Examples:
  scripts/release.sh bugfix
  scripts/release.sh feature --no-build
EOF
}

if [[ ${1:-} == "-h" || ${1:-} == "--help" || ${#} -lt 1 ]]; then
  usage
  exit 0
fi

type="$1"
shift

run_build=1
while [[ ${#} -gt 0 ]]; do
  case "$1" in
    --no-build) run_build=0; shift ;;
    *)
      echo "Unknown arg: $1" >&2
      usage
      exit 2
      ;;
  esac
done

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd -- "$script_dir/.." && pwd)"
props="$repo_root/gradle.properties"
changelog="$repo_root/CHANGELOG.md"

if [[ ! -f "$props" ]]; then
  echo "ERROR: gradle.properties not found at $props" >&2
  exit 1
fi

current_version="$(grep -E '^mod_version=' "$props" | head -n1 | cut -d= -f2-)"
if [[ -z "$current_version" ]]; then
  echo "ERROR: mod_version not found in gradle.properties" >&2
  exit 1
fi

python3 - "$type" "$props" "$changelog" <<'PY'
import re
import sys
from datetime import date

type_ = sys.argv[1]
props_path = sys.argv[2]
changelog_path = sys.argv[3]

with open(props_path, 'r', encoding='utf-8') as f:
    props = f.read()

m = re.search(r'(?m)^mod_version=(.+)$', props)
if not m:
    raise SystemExit('ERROR: mod_version not found')

cur = m.group(1).strip()
vm = re.fullmatch(r'(\d+)\.(\d+)\.(\d+)', cur)
if not vm:
    raise SystemExit(f"ERROR: mod_version is not semver x.y.z: {cur}")

major, minor, patch = map(int, vm.groups())

if type_ == 'bugfix':
    patch += 1
elif type_ == 'feature':
    major += 1
    minor = 0
    patch = 0
else:
    raise SystemExit('ERROR: type must be bugfix or feature')

new = f"{major}.{minor}.{patch}"

props_new = re.sub(r'(?m)^mod_version=.+$', f"mod_version={new}", props)
if props_new == props:
    raise SystemExit('ERROR: failed to update mod_version')

with open(props_path, 'w', encoding='utf-8') as f:
    f.write(props_new)

# Optional: add a changelog stub if CHANGELOG.md exists and the version isn't present yet.
try:
    with open(changelog_path, 'r', encoding='utf-8') as f:
        ch = f.read()
except FileNotFoundError:
    ch = None

if ch is not None and f"## [{new}]" not in ch:
    # Insert directly after the Unreleased header.
    today = date.today().isoformat()
    insert = f"\n## [{new}] - {today}\n\n### Fixed\n- TBD\n"

    # Find the first occurrence of "## [Unreleased]" and insert after it.
    unreleased = re.search(r'(?m)^## \[Unreleased\]\s*$', ch)
    if unreleased:
        pos = unreleased.end()
        ch2 = ch[:pos] + insert + ch[pos:]
        with open(changelog_path, 'w', encoding='utf-8') as f:
            f.write(ch2)

print(f"Bumped mod_version: {cur} -> {new}")
PY

if [[ $run_build -eq 1 ]]; then
  if ! command -v gradle >/dev/null 2>&1; then
    echo "ERROR: 'gradle' command not found. Install Gradle or add it to PATH." >&2
    exit 1
  fi
  echo "Running build..."
  (cd "$repo_root" && gradle build)
fi
