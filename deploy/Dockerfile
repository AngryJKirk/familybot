FROM alpine:latest

ENV SPRING_PROFILES_ACTIVE production

RUN apk --no-cache add openjdk11

WORKDIR /usr/bin/app

COPY ../build/libs/familybot.jar .

CMD ["java", "-Xms256m", "-Xmx512m","-jar", "./familybot.jar"]
