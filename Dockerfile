FROM eclipse-temurin:17
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS=""
ENV JAVA_APP_JAR="${project.artifactId}.jar"

VOLUME /tmp

RUN mkdir /app && mkdir /opt/opentelemetry
ADD target/docker-extra/opentelemetry-agent/opentelemetry-javaagent-${opentelemetry-agent.version}.jar /opt/opentelemetry/opentelemetry-javaagent.jar
COPY maven/${project.build.finalName}.jar /app/app.jar

WORKDIR /app

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
