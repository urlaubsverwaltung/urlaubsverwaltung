FROM openjdk:8-jre-alpine
EXPOSE 8080

ENV JAVA_OPTIONS=""
ENV JAVA_APP_JAR="${project.artifactId}.war"
ENV SPRING_PROFILES_ACTIVE=dev

RUN mkdir /app
ADD target/docker-extra/run-java/run-java.sh /app
ADD maven/${project.build.finalName}.war /app/${project.artifactId}.war

WORKDIR /app
CMD ./run-java.sh
