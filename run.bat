@echo off
:: Clear the console
cls

:: Run the Gradle build
echo Running Gradle build...
call gradlew clean build

:: Check if the build was successful
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed! Copying cancelled.
    pause
    exit /b %ERRORLEVEL%
)

:: Define paths
set "SOURCE_DIR=Z:\Akgamerz_790\HypexPro\build\libs"
set "DEST_DIR=C:\Users\ROCKDGYT\AppData\Roaming\.minecraft\instances\MC25\mods"

:: Clear old versions of this specific mod from the destination to prevent duplicates
echo Cleaning old versions from mods folder...
del /q "%DEST_DIR%\hypixelx-*.jar" 2>nul

:: Copy the new jar
echo Copying new JAR to Minecraft...
for %%F in ("%SOURCE_DIR%\hypixelx-*.jar") do (
    echo %%~nxF | findstr /I /C:"-sources.jar" >nul
    if errorlevel 1 (
        copy /Y "%%~fF" "%DEST_DIR%\" >nul
    )
)

echo.
echo Build and Deployment Successful!
pause
