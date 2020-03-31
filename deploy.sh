#!/bin/bash

./gradlew ktlintFormat clean build -x test &&
docker build . -f Dockerfile -t angrynaz/familybot &&
docker push angrynaz/familybot &&
ssh root@yaroslav.space '/root/familybot.sh'
