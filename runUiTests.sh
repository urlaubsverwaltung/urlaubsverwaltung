#!/usr/bin/env bash

uvServerPort=8181
export uvServerPort

echo "start server"
./mvnw clean spring-boot:run -Drun.jvmArguments="-Dserver.port=$uvServerPort" >> /dev/null &

pid=$!

echo "waiting for server to be ready"
until $(curl --output /dev/null --silent --head --fail http://localhost:$uvServerPort/login); do
    printf '.'
    sleep 5
done
echo "server started"

./node/npm run test-cafe-headless

errorlevel=$?

echo "killing server process"
kill $pid

echo "errorlevel of tests: $errorlevel"

exit $errorlevel
