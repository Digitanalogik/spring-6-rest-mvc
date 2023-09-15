FROM eclipse-temurin:17-jre-alpine

RUN apk -U upgrade

ENV JAVA_OPTS " -Xms512m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

WORKDIR application

COPY ../../../target/spring-6-rest-mvc-0.0.1-SNAPSHOT.jar ./

ENTRYPOINT ["java", "-jar", "spring-6-rest-mvc-0.0.1-SNAPSHOT.jar"]
