FROM maven:3.6.0-jdk-11-slim AS build

COPY ./pom.xml /home/app/pom.xml

# verify --fail-never works much better than dependency:resolve or dependency:go-offline
RUN mvn -f /home/app/pom.xml clean verify --fail-never

COPY ./src /home/app/src

RUN mvn -f /home/app/pom.xml package -DskipTests

FROM openjdk:11-jre-slim

ENV SPRING_PROFILES_ACTIVE production

COPY --from=build /home/app/target/familybot.jar /usr/local/lib/familybot.jar

ENTRYPOINT ["java","-jar","/usr/local/lib/familybot.jar"]
