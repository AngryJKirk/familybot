#!/bin/bash

./gradlew clean && 
./gradlew ktlintFormat build -x test &&
docker build . -f Dockerfile -t angrynaz/familybot &&
docker push angrynaz/familybot &&
ssh root@storozhenko.dev '/root/familybot.sh'
