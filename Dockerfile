FROM openjdk:8-jre-alpine

LABEL maintainer="giskard80@gmail.com"

ENV APP_ROOT /app

RUN mkdir ${APP_ROOT}

WORKDIR ${APP_ROOT}

COPY target/*.jar ${APP_ROOT}/qrcode-spring.jar
#COPY config ${APP_ROOT}/config/

#ENTRYPOINT ["java", "$JAR_OPTS", "-jar", "qrcode-spring.jar", "--server.port=$PORT"]
CMD ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","qrcode-spring.jar", "--server.port=$PORT", ">", "/dev/stdout", "2>&1"]