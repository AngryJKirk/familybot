FROM maven:3.6.0-jdk-11-slim AS build

WORKDIR /app

COPY ./pom.xml .

# verify --fail-never works much better than dependency:resolve or dependency:go-offline
RUN mvn clean verify --fail-never

COPY ./src ./src

RUN mvn package -DskipTests

FROM openjdk:11-jre-slim

ENV SPRING_PROFILES_ACTIVE production

COPY --from=build /app/target/familybot.jar /usr/local/lib/familybot.jar

ENTRYPOINT ["java","-jar","/usr/local/lib/familybot.jar"]
