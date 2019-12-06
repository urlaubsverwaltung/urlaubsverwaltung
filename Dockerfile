FROM openjdk:11-jre
EXPOSE 8080

ENV JAVA_OPTIONS=""
ENV JAVA_APP_JAR="${project.artifactId}.war"

VOLUME /tmp

RUN mkdir /app
ADD maven/${project.build.finalName}.war /app/app.war

WORKDIR /app

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.war" ]
