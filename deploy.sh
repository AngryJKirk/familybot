#!/bin/bash

gradle clean test
docker build . -f Dockerfile -t yaroslav.space:5000/familybot &&
docker push yaroslav.space:5000/familybot &&
ssh root@yaroslav.space '/root/familybot.sh'
