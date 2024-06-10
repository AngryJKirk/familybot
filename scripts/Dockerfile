FROM maven:3.9.7-eclipse-temurin-22-alpine AS build

WORKDIR /app

COPY ./pom.xml .

# verify --fail-never works much better than dependency:resolve or dependency:go-offline
RUN mvn clean verify --fail-never

COPY ./src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:22-alpine
RUN apk update && apk upgrade && apk add curl python3
ADD "https://api.github.com/repos/yt-dlp/yt-dlp/releases?per_page=1" latest_release
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp
RUN chmod a+rx /usr/local/bin/yt-dlp
ENV SPRING_PROFILES_ACTIVE production

COPY --from=build /app/target/familybot.jar /usr/local/lib/familybot.jar


ENV CMD_JMX="java -Xmx256m -Dcom.sun.management.jmxremote=true -Djava.rmi.server.hostname=localhost -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.port=9099 -Dcom.sun.management.jmxremote.rmi.port=9098 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -jar /usr/local/lib/familybot.jar"
ENV CMD_NO_JMX="java -Xmx256m -jar /usr/local/lib/familybot.jar"
ENTRYPOINT ["bash", "-c", "if [[ $ENABLE_JMX == true ]]; then exec $CMD_JMX; else exec $CMD_NO_JMX; fi"]
