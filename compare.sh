#!/bin/bash

set -e

if [ "$#" -gt 2 ]; then
    echo "Illegal number of parameters, required: max 2"
fi

docker build --build-arg JDK1="$1" --build-arg JDK2="$2" -t jdk-api-diff .
docker run --rm --entrypoint cat jdk-api-diff target/jdk-api-diff.html > ./jdk-api-diff.html