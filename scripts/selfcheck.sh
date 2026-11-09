#!/usr/bin/env bash
# Whole-pack self-check (spec AC-1..AC-4, AC-8, AC-10).
# Run from anywhere:  bash scripts/selfcheck.sh
# Checks the current branch (starters) and the `solutions` branch (via a temporary git worktree).
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if [ -z "${JAVA_HOME:-}" ] && [ -d "/c/Program Files/Java/jdk-17" ]; then export JAVA_HOME="/c/Program Files/Java/jdk-17"; fi

FAILS=0
pass() { echo "PASS  $*"; }
fail() { echo "FAIL  $*"; FAILS=$((FAILS + 1)); }
skip() { echo "SKIP  $* (not built)"; }

mvn_root() { (cd "$1" && ./mvnw -q -B "${@:2}" >"$TMP/mvn.log" 2>&1); }

TMP="$(mktemp -d)"
WT="$TMP/solutions"
cleanup() { git -C "$ROOT" worktree remove --force "$WT" >/dev/null 2>&1; rm -rf "$TMP"; }
trap cleanup EXIT

DRILLS=$(ls -d drills/d[0-9][0-9]-* 2>/dev/null)
declare -A PAPERS=( [m1]="mock-papers/M1-courier" [p1]="past-papers/P1-parking" [p2]="past-papers/P2-drone" [p3]="past-papers/P3-scheduler" )

# Per-module surefire summary: prints "<tests> <failures+errors>".
module_counts() {
  local dir="$1/target/surefire-reports" t=0 f=0
  [ -d "$dir" ] || { echo "0 0"; return; }
  for x in "$dir"/TEST-*.xml; do
    [ -e "$x" ] || continue
    local head; head=$(grep -m1 -o '<testsuite [^>]*' "$x")
    t=$((t + $(sed -E 's/.* tests="([0-9]+)".*/\1/' <<<"$head")))
    f=$((f + $(sed -E 's/.* failures="([0-9]+)".*/\1/' <<<"$head") + $(sed -E 's/.* errors="([0-9]+)".*/\1/' <<<"$head")))
  done
  echo "$t $f"
}

# ---------- AC-1: starters compile, every drill has a failing test ----------
if [ -n "$DRILLS" ]; then
  if mvn_root "$ROOT" test-compile; then pass "AC-1 drills compile on $(git branch --show-current)"; else fail "AC-1 drills do not compile"; tail -20 "$TMP/mvn.log"; fi
  mvn_root "$ROOT" test -Dmaven.test.failure.ignore=true || true
  for d in $DRILLS; do
    read -r t f < <(module_counts "$d")
    if [ "$t" -gt 0 ] && [ "$f" -gt 0 ]; then pass "AC-1 $d starter fails $f/$t tests (unsolved, as intended)"
    else fail "AC-1 $d starter: $f failing of $t tests (an untouched starter must fail)"; fi
  done
else skip "AC-1 drills"; fi
for id in "${!PAPERS[@]}"; do
  p="${PAPERS[$id]}"; [ -f "$p/pom.xml" ] || { skip "AC-1 $p"; continue; }
  if mvn_root "$p" test-compile; then pass "AC-1 $p compiles"; else fail "AC-1 $p does not compile"; fi
done

# ---------- solutions worktree ----------
if git show-ref --verify --quiet refs/heads/solutions; then
  if git merge-base --is-ancestor HEAD solutions; then pass "solutions contains $(git branch --show-current)"
  else fail "solutions is behind $(git branch --show-current): run  git switch solutions && git merge $(git branch --show-current)"; fi
  git worktree add --force "$WT" solutions >/dev/null 2>&1 || fail "could not create solutions worktree"

  # ---------- AC-2: all drill tests pass on solutions ----------
  if [ -n "$DRILLS" ]; then
    if mvn_root "$WT" test; then pass "AC-2 every drill test passes on solutions"
    else fail "AC-2 drill tests fail on solutions"; grep -E "Tests run:.*Fail|FAIL|ERROR\]" "$TMP/mvn.log" | head -20; fi
  fi

  # ---------- AC-3/AC-4: marking scores ----------
  for id in m1 p1 p2 p3; do
    p="${PAPERS[$id]}"
    if [ ! -d "marking/$id" ] || [ ! -f "$p/pom.xml" ]; then skip "AC-3 $id marking"; continue; fi
    for side in solutions starter; do
      src="$WT/$p"; [ "$side" = starter ] && src="$ROOT/$p"
      copy="$TMP/mark-$id-$side"; rm -rf "$copy"; cp -r "$src" "$copy"; rm -rf "$copy/target" "$copy/src/test/java/marking"
      out=$(java "-Dmarking.home=$ROOT/marking" "$ROOT/marking/Mark.java" "$id" "$copy" 2>&1); code=$?
      if [ $code -eq 2 ]; then fail "AC-4 $id marks do not add up: $(grep 'MARKING ERROR' <<<"$out" | head -3)"; break; fi
      total=$(grep -oE 'TOTAL +[0-9]+' <<<"$out" | grep -oE '[0-9]+$'); total=${total:-0}
      if [ "$side" = solutions ]; then
        if [ "$total" -eq 100 ]; then pass "AC-3 $id solution scores 100/100"; else fail "AC-3 $id solution scores $total/100"; grep -E '^\s+\[ \]' <<<"$out" | head -10; fi
      else
        if [ "$total" -le 20 ]; then pass "AC-3 $id untouched starter scores $total/100 (≤ 20)"; else fail "AC-3 $id untouched starter scores $total/100 (> 20: tests too loose)"; fi
      fi
    done
  done

  # ---------- AC-10: branches differ only in src/main ----------
  bad=$(git diff --name-only HEAD solutions | grep -vE '(^|/)src/main/' | grep -vE '^SOLUTIONS\.md$|/SOLUTION-NOTES\.md$' || true)
  if [ -z "$bad" ]; then pass "AC-10 solutions differs only in src/main"; else fail "AC-10 solutions also changes: $(tr '\n' ' ' <<<"$bad")"; fi
else
  skip "solutions branch"
fi

# ---------- AC-8: lessons and paper front matter ----------
for d in $DRILLS; do
  l="$d/LESSON.md"
  if [ ! -f "$l" ]; then fail "AC-8 $l missing"; continue; fi
  missing=""
  for h in "## The idea" "## Worked example" "## Your task" "## Hints" "## Viva questions"; do grep -qF "$h" "$l" || missing="$missing [$h]"; done
  n=$(grep -c '<details>' "$l")
  if [ -z "$missing" ] && [ "$n" -ge 6 ]; then pass "AC-8 $l sections ok ($n details blocks)"; else fail "AC-8 $l missing:$missing details=$n (need ≥6: 3 hints + 3 viva)"; fi
done
for f in papers/*.md; do
  [ -e "$f" ] || { skip "AC-8 papers"; break; }
  fm=$(awk 'NR==1 && $0!="---"{exit} NR>1 && $0=="---"{exit} NR>1{print}' "$f")
  missing=""
  for k in id module kind title difficulty timeMinutes tasks; do grep -qE "^$k:" <<<"$fm" || missing="$missing $k"; done
  if [ -z "$missing" ]; then pass "AC-8 $f front matter ok"; else fail "AC-8 $f front matter missing:$missing"; fi
done

echo
if [ "$FAILS" -eq 0 ]; then echo "SELFCHECK OK"; exit 0; else echo "SELFCHECK FAILED ($FAILS)"; exit 1; fi
