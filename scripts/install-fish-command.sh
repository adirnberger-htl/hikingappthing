#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
functions_dir="${XDG_CONFIG_HOME:-$HOME/.config}/fish/functions"
target="$functions_dir/hikingappthing.fish"
source_template="$repo_dir/scripts/hikingappthing.fish"

mkdir -p "$functions_dir"

{
  echo "set -gx HIKING_APP_REPO \"$repo_dir\""
  cat "$source_template"
} > "$target"

echo "Installed Fish command: hikingappthing"
echo "File: $target"
