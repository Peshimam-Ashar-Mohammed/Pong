
@echo off
title PONG Launcher

cd /d "%~dp0"

if not exist out mkdir out

echo Compiling Pong...

javac -d out src\Pong.java

if errorlevel 1 (
    echo.
    echo Compilation failed!
    pause
    exit /b 1
)

echo Launching Pong...

start "" javaw -cp out Pong

exit
