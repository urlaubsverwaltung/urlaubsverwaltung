#!/usr/bin/env bash
echo "==> going to release version=$RELEASE_VERSION"
echo "==> new development version will be $NEW_VERSION"

./mvnw --version
./mvnw --batch-mode release:prepare \
 -Dresume=false \
 -DautoVersionSubmodules=true \
 -Dtag=$RELEASE_VERSION \
 -DreleaseVersion=$RELEASE_VERSION\
 -DdevelopmentVersion=$NEW_VERSION

./mvnw release:clean

