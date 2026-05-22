@echo off
REM Direct Fraynix Boot Script - Bypasses Gradle console issues
echo Starting Fraynix Boot with proper console...

REM Set Java options for proper console handling
set JAVA_OPTS=-Xmx2g -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8

REM Build if needed
if not exist "build\classes\java\main" (
    echo Building Fraynix...
    call ..\gradlew.bat compileJava -q
)

REM Get classpath from Gradle
for /f "delims=" %%i in ('..\gradlew.bat printClasspath -q 2^>nul') do set CLASSPATH=%%i

REM Run directly with Java for proper console interaction
echo Launching Fraynix Boot...
java %JAVA_OPTS% -cp "%CLASSPATH%" fraymus.FraynixBoot %*
