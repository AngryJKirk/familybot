FROM maven:3.9.7-eclipse-temurin-22-alpine AS build

WORKDIR /app
RUN apk update && apk upgrade && apk add git
RUN git clone https://github.com/AngryJKirk/TelegramBots.git && cd TelegramBots && mvn -q install -Dgpg.skip

COPY ./pom.xml .

# verify --fail-never works much better than dependency:resolve or dependency:go-offline
RUN mvn clean verify --fail-never

COPY ./src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:22-alpine
RUN apk update && apk upgrade && apk add curl python3 bash
ADD "https://api.github.com/repos/yt-dlp/yt-dlp/releases?per_page=1" latest_release
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp
RUN chmod a+rx /usr/local/bin/yt-dlp
ENV SPRING_PROFILES_ACTIVE production

COPY --from=build /app/target/familybot.jar /usr/local/lib/familybot.jar


ENV CMD_JFR="java -Xmx256m -XX:StartFlightRecording=disk=false,duration=120m,maxage=120m -jar /usr/local/lib/familybot.jar"
ENV CMD_NO_JFR="java -Xmx256m -jar /usr/local/lib/familybot.jar"
ENTRYPOINT ["bash", "-c", "if [[ $ENABLE_JFR == true ]]; then exec $CMD_JFR; else exec $CMD_NO_JFR; fi"]
