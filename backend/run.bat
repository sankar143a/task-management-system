@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.12
set PATH=%JAVA_HOME%\bin;%PATH%

echo Starting server...

java -cp "bin;lib\gson-2.8.9.jar;lib\mysql-connector-j-9.6.0.jar" Main

pause