FROM openjdk:8-jre-alpine

LABEL maintainer="giskard80@gmail.com"

ENV APP_ROOT /app

RUN mkdir ${APP_ROOT}

WORKDIR ${APP_ROOT}

COPY target/*.jar ${APP_ROOT}/qrcode-spring.jar

# Fire up our Spring Boot app by default
CMD [ "sh", "-c", "java $JAR_OPTS -Dserver.port=$PORT  -Djava.security.egd=file:/dev/./urandom -jar qrcode-spring.jar" ]
