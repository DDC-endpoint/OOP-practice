#!/usr/bin/env bash
# Copies every LESSON.md / PAPER.md (which carry the front matter) into papers/<id>.md for the study site.
# Run after editing a lesson or paper:  bash scripts/sync-papers.sh
set -eu
ROOT="$(cd "$(dirname "$0")/.." && pwd)"; cd "$ROOT"; mkdir -p papers
for f in drills/*/LESSON.md mock-papers/*/PAPER.md past-papers/*/PAPER.md; do
  [ -e "$f" ] || continue
  id=$(awk 'NR==1 && $0!="---"{exit} /^id:/{print $2; exit}' "$f")
  [ -n "$id" ] || { echo "no id in $f" >&2; exit 1; }
  { cat "$f"; printf '\n<!-- generated from %s by scripts/sync-papers.sh; edit the source, not this file -->\n' "$f"; } > "papers/$id.md"
  echo "papers/$id.md <- $f"
done
