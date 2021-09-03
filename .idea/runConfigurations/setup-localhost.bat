@echo off

set VERSION=1.16.5
set BUILD=768

echo Setting Up localhost server
echo Looking for server jar

if not exist run\server-1\ mkdir run\server-1
cd run\server-1

if exist server.jar goto finishedDl
echo Downloading paper...

powershell -Command "Invoke-WebRequest https://papermc.io/api/v2/projects/paper/versions/%VERSION%/builds/%BUILD%/downloads/paper-%VERSION%-%BUILD%.jar -OutFile server.jar"

:finishedDl
type nul > eula.txt
echo eula=true> eula.txt
echo Finished!

cd ..
cd ..