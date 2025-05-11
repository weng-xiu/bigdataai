@echo off
cd backend
mvn exec:java -Dexec.mainClass="com.bigdataai.TestApplication"
pause