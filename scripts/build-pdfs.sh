#!/usr/bin/env bash
# Rebuilds every LESSON.pdf / HINTS.pdf (drills) and PAPER.pdf (papers), then checks them.
# Run from anywhere:  bash scripts/build-pdfs.sh [filter]     e.g.  bash scripts/build-pdfs.sh d04
# Needs Node 18+ and Chrome or Edge (set CHROME_PATH if it isn't in the usual place).
set -eu
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/scripts/pdf"
[ -d node_modules ] || PUPPETEER_SKIP_DOWNLOAD=1 npm ci --no-fund --no-audit
node build.mjs "$@"
node check.mjs
