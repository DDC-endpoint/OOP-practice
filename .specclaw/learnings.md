# Learnings: 002-drill-paper-pdfs

Build learnings, spec gaps, and patterns discovered.

**Categories:** spec_gap | design_gap | pattern | best_practice | agent_issue

---

## [L1] design_gap — File scripts/pdf/lib.mjs (shared parser for build.mjs + c...

**When:** 2026-11-12 17:08 UTC
**Category:** design_gap
**Priority:** low
**Status:** pending

### Detail
File scripts/pdf/lib.mjs (shared parser for build.mjs + check.mjs) added but not in T1's declared files or the spec file map

### Action
Declare shared helper modules in the Approach file map when two scripts share parsing

---

## [L2] pattern — Chrome paints position:relative/absolute elements last, s...

**When:** 2026-11-12 17:08 UTC
**Category:** pattern
**Priority:** medium
**Status:** pending

### Detail
Chrome paints position:relative/absolute elements last, so their text comes out of order in PDF text extraction; and PDF text has ligatures (fl) that need NFKC

### Action
For print CSS whose PDF text is machine-checked, avoid positioned elements and normalise extracted text with NFKC

---

## [L3] design_gap — scripts/pdf/lib.mjs added but not declared in T1 files or...

**When:** 2026-11-12 17:08 UTC
**Category:** design_gap
**Priority:** low
**Status:** pending

### Detail
scripts/pdf/lib.mjs added but not declared in T1 files or the spec file map

### Action
Declare shared helper modules in the Approach file map

---
