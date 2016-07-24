#!/bin/bash
MYSELF=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"
java=java
java_args="-Dfile.encoding=UTF-8"
if test -n "$JAVA_HOME"; then
    java="$JAVA_HOME/bin/java"
fi

if [[ $@ == *--debug* ]] ; then
	java_args="$java_args -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" 
	echo "Debugging"
fi

exec "$java" $java_args $JAVA_OPTS -DSANDBOX_VERSION=$SANDBOX_VERSION -jar $MYSELF "$@"
exit $?