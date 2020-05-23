#!/bin/bash
set -e
gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS

gsutil cp ./cli/build/graal/sandbox-worker-cli-linux-x86_64 gs://sandbox-releases/worker-cli/worker-cli-linux-x86_64-$BUILD_VERSION
echo "Uploaded sandbox-worker-cli-linux-x86_64 to: https://storage.cloud.google.com/sandbox-releases/worker-cli/worker-cli-linux-x86_64-$BUILD_VERSION"

(cd ./cli; docker build . -t getsandbox/worker-cli:$BUILD_VERSION)
docker login -u $DOCKERUSER -p $DOCKERPASS
docker push getsandbox/worker-cli:$BUILD_VERSION
echo "Pushed docker image to getsandbox/worker-cli:$BUILD_VERSION"

gsutil cp ./cli/build/libs/worker.cli-all.jar gs://sandbox-releases/worker-cli/worker-cli-$BUILD_VERSION-all.jar
echo "Uploaded worker.cli-all.jar to: https://storage.cloud.google.com/sandbox-releases/worker-cli/worker-cli-$BUILD_VERSION-all.jar"