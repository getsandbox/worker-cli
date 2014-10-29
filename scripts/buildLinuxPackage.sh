#!/bin/bash
app_path=$1
output_path=$2
run_script=$app_path/scripts/linuxEmbeddedRun.sh
package_path=$output_path/sandbox

if [ -z "$2" ]; then
	echo "Arguments are: <git root> <output dir>"
	exit 1
fi

set -e

# build jar
(cd $app_path/; gradle clean build shadowJar)

# package it up
echo "Creating runnable package: $package_path"
cat $run_script $app_path/build/libs/*-1.0-all.jar > $package_path && chmod +x $package_path
(cd $output_path; tar -cf sandbox.tar sandbox)
