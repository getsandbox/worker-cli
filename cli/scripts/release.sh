#!/bin/bash
set -e
gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS

gsutil cp -a public-read ./cli/build/graal/sandbox-worker-cli-linux-x86_64 gs://sandbox-releases/worker-cli/worker-cli-linux-x86_64-$BUILD_VERSION
gsutil cp -a public-read gs://sandbox-releases/worker-cli/worker-cli-linux-x86_64-$BUILD_VERSION gs://sandbox-releases/worker-cli/worker-cli-linux-x86_64-latest
echo "Uploaded sandbox-worker-cli-linux-x86_64 to: https://storage.cloud.google.com/sandbox-releases/worker-cli/worker-cli-linux-x86_64-$BUILD_VERSION"

(cd ./cli; docker build . -t getsandbox/worker-cli:$BUILD_VERSION -t getsandbox/worker-cli:latest)
docker login -u $DOCKERUSER -p $DOCKERPASS
docker push getsandbox/worker-cli:$BUILD_VERSION
docker push getsandbox/worker-cli:latest
echo "Pushed docker image to getsandbox/worker-cli:$BUILD_VERSION"

gsutil cp -a public-read ./cli/build/libs/worker.cli-all.jar gs://sandbox-releases/worker-cli/worker-cli-$BUILD_VERSION.jar
gsutil cp -a public-read gs://sandbox-releases/worker-cli/worker-cli-$BUILD_VERSION-all.jar gs://sandbox-releases/worker-cli/worker-cli-latest.jar
echo "Uploaded worker.cli-all.jar to: https://storage.cloud.google.com/sandbox-releases/worker-cli/worker-cli-$BUILD_VERSION.jar"