FROM eclipse-temurin:17
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS=""
ENV JAVA_APP_JAR="${project.artifactId}.war"

VOLUME /tmp

RUN mkdir /app
COPY maven/${project.build.finalName}.war /app/app.war

WORKDIR /app

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.war"]
