FROM alpine:latest

ENV SPRING_PROFILES_ACTIVE production

RUN apk --no-cache add openjdk11

WORKDIR /usr/bin/app

COPY . .

RUN ./gradlew build -x test

EXPOSE 8080 8085

CMD ["./gradlew", "bootRun"]
