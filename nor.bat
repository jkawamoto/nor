@echo off

set CLASSPATH=.;nor.jar
for %%i in (.\plugin\*) do call :setpath %%i
for /D %%i in (.\plugin\*) do call :setpath %%i
goto :endsubs
:setpath
set CLASSPATH=%CLASSPATH%;%1
goto :EOF
:endsubs

echo %CLASSPATH%

echo Nor (ver. 0.1)
java -classpath "%CLASSPATH%" nor.core.Nor

