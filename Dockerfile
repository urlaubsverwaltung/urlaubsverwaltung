FROM openjdk:8-jre-alpine

EXPOSE 8080

ENV JVM_OPTS=""
ENV JAVA_OPTS=""


RUN apk add --no-cache curl

RUN mkdir /app
ADD /maven/${project.build.finalName}.jar /app/${project.artifactId}.jar

WORKDIR /app
CMD /usr/bin/java $JVM_OPTS -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar /app/${project.artifactId}.jar
