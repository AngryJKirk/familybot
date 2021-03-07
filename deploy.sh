#!/bin/bash
./gradlew clean &&
./gradlew ktlintFormat build -x test &&
docker build . -f Dockerfile -t angrynaz/familybot &&
docker rm -f suchara
echo 'Bot has been stopped' &&
docker run -d --restart always --net suchara-net -p 127.0.0.1:8080:8080 --name suchara angrynaz/familybot:latest &&
echo 'Everything is done, master'
