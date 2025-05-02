FROM amazoncorretto:17-alpine3.21-jdk
RUN apk --no-cache add curl wget

ENV JAVA_OPTS=""
ENV APP_OPTS=""
WORKDIR /app

COPY ./build/libs/lib ./lib
COPY ./build/libs/app.jar ./app.jar

EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -XX:+UseG1GC -server -cp "app.jar:lib/*" io.ktor.server.netty.EngineMain -port=8080 $APP_OPTS

# docker build --platform linux/amd64