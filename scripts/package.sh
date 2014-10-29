#!/bin/bash
WORKING_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set -e

# get java 8 just to be sure
(mkdir -p /tmp/java8; cd /tmp/java8; wget --no-check-certificate -c --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u25-b17/jdk-8u25-linux-x64.tar.gz)
(cd /tmp/java8; gzip -d jdk-8u25-linux-x64.tar.gz; tar -xf jdk-8u25-linux-x64.tar)
export JAVA_HOME=/tmp/java8/jdk1.8.0_25

# install aws tools
pip install awscli > /dev/null

# build runtime binary
$WORKING_DIR/buildLinuxPackage.sh $WORKING_DIR/.. /tmp

# upload built binary to s3 and make it public
aws s3 cp /tmp/sandbox.tar s3://getsandbox-assets/runtime-binary.tar --acl public-read