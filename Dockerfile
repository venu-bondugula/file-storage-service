FROM openjdk:17-alpine
ADD target/file-storage-server.jar file-storage-server.jar
ADD data data
EXPOSE 8080
ENTRYPOINT [ "java", "-jar",  "file-storage-server.jar"]