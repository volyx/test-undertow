FROM	openjdk:8-jre-alpine

COPY rest/build/libs/rest-1.0-SNAPSHOT-all.jar /
COPY rest/build/resources/main/request.config /

EXPOSE 8080:8080
ENTRYPOINT ["java", "-Denv=prod", "-jar", "rest-1.0-SNAPSHOT-all.jar", "-config", "request.config"]
