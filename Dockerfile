FROM openjdk:11-jdk

WORKDIR /usr/bin/app

COPY . .

RUN ./gradlew build -x test

EXPOSE 8080 8085

CMD ["./gradlew", "bootRun"]
