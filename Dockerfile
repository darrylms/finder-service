FROM openjdk:8u171-alpine3.7
RUN apk --no-cache add curl
COPY target/finder-service*.jar finder-service.jar
CMD java ${JAVA_OPTS} -jar finder-service.jar