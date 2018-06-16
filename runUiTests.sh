#!/usr/bin/env bash

serverPort=8181
export serverPort

echo "start server"
./mvnw clean spring-boot:run -Drun.jvmArguments="-Dserver.port=$serverPort" >> /dev/null &

pid=$!

echo "waiting for server to be ready"
until $(curl --output /dev/null --silent --head --fail http://localhost:$serverPort/login); do
    printf '.'
    sleep 5
done
echo "server started"

npm run test-cafe-headless

errorlevel=$?

echo "killing server process"
kill $pid

echo "errorlevel of tests: $errorlevel"

exit $errorlevel
