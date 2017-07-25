#!/bin/bash
WORKING_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

git pull --unshallow

sandbox_git_version=`git rev-list --all HEAD | wc -l`
sandbox_sha=`git rev-parse --short HEAD`
sandbox_version="1.0.$sandbox_git_version"

set -e

jdk_switcher use oraclejdk8

# install aws tools
pip install awscli > /dev/null

# build runtime binary
$WORKING_DIR/buildLinuxPackage.sh $WORKING_DIR/.. /tmp $sandbox_version $sandbox_sha

# upload built binary to s3 and make it public
aws s3 cp /tmp/sandbox.tar s3://sandbox-binaries/runtime-binary.tar --acl public-read