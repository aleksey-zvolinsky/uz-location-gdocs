@echo off
call env.bat

set JAR=uz-location-0.0.1-SNAPSHOT.jar
rem set CLASSPATH=./config/*;kerriline-1563474005a6.p12;.
set LOADER_PATH=%UZ_HOME%/config
"%JAVA_HOME%/bin/java.exe" -jar %JAR% -Duser.country=US -Duser.language=en