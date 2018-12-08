FROM fabric8/java-alpine-openjdk8-jre:latest

ENV JAVA_APP_DIR=/app

WORKDIR /app

COPY /maven/${docker.finalName}.jar /app/${project.artifactId}.jar

EXPOSE 8080
