@echo off
call env.bat

set JAR=uz-location-0.0.1-SNAPSHOT.jar
set LOADER_PATH=%UZ_HOME%/config
"%JAVA_HOME%/bin/java.exe" -jar %JAR% -Duser.country=US -Duser.language=en