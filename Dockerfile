FROM openjdk:8u171-jre-slim
ADD https://dl.bintray.com/getsandbox/public/com/sandbox/sandbox/1.0.230/sandbox-1.0.230-all.jar /sandbox.jar
ENV LANG C.UTF-8
CMD java -XX:+UseG1GC -XX:+UseStringDeduplication ${JAVA_OPTS:--Xmx128m -Xmx128m} -jar /sandbox.jar --base=/base --port=${PORT:-80} --watch=false --metadataPort=${METADATA_PORT:-10000} --metadataLimit=${METADATA_LIMIT:-250} run
