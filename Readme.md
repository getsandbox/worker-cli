# Sandbox Worker CLI (Runtime v2)

[Sandbox](https://getsandbox.com) is a platform to quickly and easily create or generate web service mocks, with instant deploy, collaborative build, and debugging tools for API developers. [More info - https://getsandbox.com](https://getsandbox.com)

Sandbox Runtime is the core processing component of the Sandbox product, it is responsible for executing HTTP requests against your definition files (main.js etc) and templates (template.liquid) to produce a response. 
The intent of this component is to reduce the feedback loop of making change to seeing the output during local development, and also to support CI test execution. 

There are three distribution types:
- Statically linked linux binary (no JRE etc required built via GraalVM, linux only)
- Docker container (needs docker, linux and mac) - https://hub.docker.com/r/getsandbox/worker-cli
- Fat JAR (needs Java JRE, windows, linux and mac) 

## Getting Started

[Getting Started](https://getsandbox.com/docs/getting-started) and the [API Definition](https://getsandbox.com/docs/sandbox-api) pages.

## Binaries

Docker: https://hub.docker.com/r/getsandbox/worker-cli

Linux native image: https://storage.cloud.google.com/sandbox-releases/worker-cli/worker-cli-linux-x86_64-latest

Fat JAR: https://storage.cloud.google.com/sandbox-releases/worker-cli/worker-cli-latest.jar


## Usage

```
Usage: sandbox [--quiet] [--verbose] [--watch] [--base=<basePath>]
               [--metadataLimit=<activityStorageLimit>]
               [--metadataPort=<activityListenerPort>]
               [--port=<requestListenerPort>] [--runtimeVersion=<version>]
               [--state=<statePath>]
      --base=<basePath>     The directory to try and load the Sandbox JS definition
                              from
      --metadataLimit, --activityLimit=<activityStorageLimit>
                            The number of activity messages to keep in-memory before
                              they get discarded
      --metadataPort, --activityPort=<activityListenerPort>
                            The port to optionally start the activity api on, can be
                              used to introspect what requests are hitting the
                              server, useful for CI assertions
      --port=<requestListenerPort>
                            The port to listen on for requests
      --quiet               Reduce logging, request / response and console.log()
                              won't be shown, only errors.
      --runtimeVersion=<version>
                            The runtime version to execute at, the version effects
                              what libraries are injected and what ECMAScript
                              version is available
      --state=<statePath>   The file to load and store the Sandbox state object to,
                              by default state will only exist ephemerally in-memory
      --verbose             Increase logging, request / response bodies will be shown
      --watch               Whether to watch the base path for changes and
                              automatically reload or not
```

## License

MIT
