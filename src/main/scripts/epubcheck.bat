@ECHO OFF

REM If JAVA_HOME is not set, use the java in the execution path
if "%JAVA_HOME%" == "" (
   set JAVA=java
) else (
   set JAVA="%JAVA_HOME%\bin\java"
)
SET EPUBCHECK_HOME=%~dp0

REM Loop to read all the arguments
SET ARGS=
:WHILE
IF "%1"=="" GOTO LOOP
  SET ARGS=%ARGS% %1
  SHIFT
  GOTO WHILE
:LOOP

"%JAVA%" -jar "%EPUBCHECK_HOME%\epubcheck-${project.version}.jar" %ARGS%
