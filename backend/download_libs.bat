@echo off
echo Downloading required libraries...

if not exist "lib" mkdir lib

echo Downloading Gson library...
powershell -Command "Invoke-WebRequest -Uri 'https://search.maven.org/remotecontent?filepath=com/google/code/gson/gson/2.8.9/gson-2.8.9.jar' -OutFile 'lib\gson-2.8.9.jar'"

echo Downloading MySQL Connector...
powershell -Command "Invoke-WebRequest -Uri 'https://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar' -OutFile 'lib\mysql-connector-java-8.0.33.jar'"

echo Done!
dir lib
pause