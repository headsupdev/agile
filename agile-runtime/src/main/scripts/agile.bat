@echo off

:: Try to get around "Terminate batch job?" - use start, which spawns a new window
:: Establish if we were launched from the command line.
:: If so, run a start /wait otherwise we want the original window to close
set SCRIPT=%0
set DQUOTE="
@echo %SCRIPT:~0,1% | findstr /l %DQUOTE% > NUL
if NOT %ERRORLEVEL% EQU 0 set START_PARAMS=/wait

set OLDDIR=%CD%
cd %~dp0
cd ..
set AGILE_HOME=%CD%

set CMD_LINE_ARGS=
:getArgs
if %1x==x goto doneArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto getArgs
:doneArgs

SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"

set JAVA_OPTS=-Xmx512m -Djava.awt.headless=true -Duser.timezone=UTC

cd %AGILE_HOME%

@start %START_PARAMS% cmd /c %JAVA_EXE% %JAVA_OPTS% -cp conf;bin\${project.artifactId}-${project.version}.jar org.headsupdev.agile.runtime.Main %CMD_LINE_ARGS%

set CMD_LINE_ARGS=
set JAVA_EXE=
set JAVA_OPTS=

cd %OLDDIR%
set OLDDIR=
