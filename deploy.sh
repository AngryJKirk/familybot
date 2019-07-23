#!/bin/bash


docker build . -f Dockerfile -t angrynaz/familybot &&
docker push angrynaz/familybot &&
ssh root@yaroslav.space '/root/familybot.sh'
