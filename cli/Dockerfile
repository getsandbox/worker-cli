FROM bitnami/minideb:stretch
ADD build/graal/sandbox-worker-cli-linux-x86_64 /sandbox-worker-cli
ENV LANG C.UTF-8
CMD /sandbox-worker-cli ${MEMORY_OPTS:--Xmx128m -Xmx128m -Xss128k} ${JAVA_OPTS:--Dmicronaut.server.netty.worker.threads=2} --base=/base --port=${PORT:-80} --watch=false ${JAVA_PARAMS} run