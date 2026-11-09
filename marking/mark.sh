#!/usr/bin/env bash
# Mark a practice-paper attempt.  Usage: marking/mark.sh <m1|p1|p2|p3> <projectDir>
HERE="$(cd "$(dirname "$0")" && pwd)"
exec java "-Dmarking.home=$HERE" "$HERE/Mark.java" "$@"
