#!/bin/bash
set -e

WORKING_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_PATH="$WORKING_DIR/.."
SANDBOX_VERSION="$TRAVIS_BRANCH"
OUTPUT_PATH="./build"
PACKAGE_PATH=$OUTPUT_PATH/sandbox-runtime
RUN_SCRIPT=$WORKING_DIR/linuxEmbeddedRun.sh

# build jar
./gradlew clean build shadowJar -Dsandbox_version="$SANDBOX_VERSION"

# package it up
echo "Creating runnable package: $PACKAGE_PATH"
printf "#!/bin/bash\nSANDBOX_VERSION='$SANDBOX_VERSION'\n" > $OUTPUT_PATH/version
cat $OUTPUT_PATH/version $RUN_SCRIPT $APP_PATH/build/libs/*-all.jar > $PACKAGE_PATH && chmod +x $PACKAGE_PATH
(cd $OUTPUT_PATH; tar -cf sandbox-runtime.tar sandbox-runtime)
