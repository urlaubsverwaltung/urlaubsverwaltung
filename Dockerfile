FROM adoptopenjdk:11-jre-hotspot
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS=""
ENV JAVA_APP_JAR="${project.artifactId}.war"

VOLUME /tmp

RUN mkdir /app && mkdir /opt/opentelemetry
ADD target/docker-extra/opentelemetry-agent/opentelemetry-javaagent-${opentelemetry-agent.version}.jar /opt/opentelemetry/opentelemetry-javaagent.jar
COPY maven/${project.build.finalName}.war /app/app.war

WORKDIR /app

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.war"]
