#!/bin/bash
set -e
echo "version=$BUILD_VERSION" >> ./cli/src/main/resources/values.properties
./gradlew jar shadowJar nativeImage test