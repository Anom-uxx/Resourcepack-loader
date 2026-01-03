#!/usr/bin/env bash
set -euo pipefail

# Wrapper for: scripts/release.sh bugfix
script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

exec bash "$script_dir/release.sh" bugfix "$@"
