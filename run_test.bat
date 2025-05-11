@echo off
cd backend

:: 先构建项目生成JAR包
call mvn clean package -DskipTests

:: 使用java -jar命令运行应用
:: 注意：虽然pom.xml中配置的主类是BigDataApplication，但我们需要运行TestApplication
java -jar target\bigdata-ai-backend.jar -Dspring.main.sources=com.bigdataai.TestApplication --spring.main.web-application-type=none
pause