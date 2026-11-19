@echo off
rem Rebuilds every LESSON.pdf / HINTS.pdf (drills), PAPER.pdf (papers) and guides/*.pdf, then checks them.
rem Run from anywhere:  scripts\build-pdfs.cmd [filter]     e.g.  scripts\build-pdfs.cmd d04
rem Needs Node 18+ and Chrome or Edge (set CHROME_PATH if it isn't in the usual place).
setlocal
pushd "%~dp0pdf" || exit /b 1
if not exist node_modules (
  set PUPPETEER_SKIP_DOWNLOAD=1
  call npm ci --no-fund --no-audit || (popd & exit /b 1)
)
node build.mjs %* || (popd & exit /b 1)
node check.mjs
set RC=%ERRORLEVEL%
popd
exit /b %RC%
