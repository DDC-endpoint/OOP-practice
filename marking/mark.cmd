@echo off
rem Mark a practice-paper attempt.  Usage: marking\mark.cmd ^<m1^|p1^|p2^|p3^> ^<projectDir^>
java "-Dmarking.home=%~dp0." "%~dp0Mark.java" %*
