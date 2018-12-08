#!/bin/bash

echo "Login into docker.io"
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
./mvnw docker:push
