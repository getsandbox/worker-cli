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
sandbox_version=`git rev-list --all HEAD | wc -l`
sandbox_sha=`git rev-parse --short HEAD`
printf "#!/bin/bash\nSANDBOX_VERSION='1.$sandbox_version-$sandbox_sha'\n" > $output_path/version
cat $output_path/version $run_script $app_path/build/libs/*-1.0-all.jar > $package_path && chmod +x $package_path
(cd $output_path; tar -cf sandbox.tar sandbox)
