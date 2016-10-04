package com.sandbox.runtime.config;

import com.sandbox.runtime.js.models.RuntimeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import java.nio.file.Paths;

@Configuration
@ComponentScan(basePackages = {"com.sandbox.runtime"},
        excludeFilters = { @ComponentScan.Filter( Configuration.class ) }
)
public class CLIBootstrap extends Context {

    private static Logger logger = LoggerFactory.getLogger(CLIBootstrap.class);

    public static void main(String[] args) {
        //process command line args and kick off
        try {
            Context.config = getConfig(new SimpleCommandLinePropertySource(args));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            System.exit(-1);
        }

        try {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CLIBootstrap.class);
            context.start();
            //start server
            context.getBean(Context.class).start();

        }catch(Throwable e){
            logger.error("Error starting runtime");
            unwrapException(e).printStackTrace();
            System.exit(1);
        }
    }

    public static Config getConfig(SimpleCommandLinePropertySource source) {
        Config config = new Config();

        //base path options, fallback to current working direct --base=<dir>
        String basePathStr = getProperty(source, "base", String.class);
        if(basePathStr != null){
            config.setBasePath(Paths.get(basePathStr));
        }

        //state path, where we will read/persist state data to
        String statePathStr = getProperty(source, "state", String.class);
        if(statePathStr != null){
            config.setStatePath(Paths.get(statePathStr));
        }

        //set runtime version, needs to be a valid enum
        String runtimeVersionStr = getProperty(source, "runtimeVersion", String.class, RuntimeVersion.getLatest().toString());
        try{
            config.setRuntimeVersion(RuntimeVersion.valueOf(runtimeVersionStr));
        }catch (Exception e){
            throw new IllegalArgumentException("Invalid runtime version");
        }

        config.setHttpPort(getProperty(source, "port", Integer.class, 8080));
        config.setDebugPort(5005);//getProperty(source, "debug",Integer.class, 5005);
        config.setVerboseLogging(getProperty(source, "verbose", String.class) == null ? false : true);
        config.setDisableLogging(getProperty(source, "quiet", String.class) == null ? false : true);
        config.setDisableIDs(getProperty(source, "disableIDs", String.class) == null ? false : true);
        config.setEnableConcurrency(getProperty(source, "enableConcurrency", String.class) == null ? false : true);
        config.setDisableRefresh(getProperty(source, "disableRefresh", String.class) == null ? false : true);

        String command = getProperty(source, "nonOptionArgs");
        if (command == null || command.isEmpty() || !"run".equals(command)) {
            showValidArguments();
            System.exit(1);
        }

        return config;
    }

    private static String getProperty(PropertySource property, String key){
        return getProperty(property, key, String.class, null);
    }

    private static <T> T getProperty(PropertySource property, String key, Class<T> returnType){
        return getProperty(property, key, returnType, null);
    }

    private static <T> T getProperty(PropertySource property, String key, Class<T> returnType, T defaultValue){
        String value = (String) property.getProperty(key);
        if(value == null) return defaultValue;
        if(returnType == String.class) return (T) value;
        if(returnType == Integer.class) return (T) Integer.valueOf(value);
        throw new IllegalArgumentException("Unsupported return type: " + returnType.getName());
    }

    private static void showValidArguments(){
        logger.info("No valid argument specified.\n\n" +
                        "Commands:\n" +
                        "run\t\t Starts a sandbox runtime in the current working directory.\n" +
                        "\n" +
                        "Options:\n" +
                        "--port=<port number>\n" +
                        "--base=<base directory> (Overrides working directory)\n" +
                        "--state=<file to persist state to> (Reads/writes a file to persist state across runs)\n" +
                        "--runtimeVersion=" + RuntimeVersion.toCLIString() + "\n" +
                        "--verbose (Increases logging verbosity, full request and response bodies etc)\n" +
                        "--quiet (Disables console logging, overrides verbose if specified)\n" +
                        //                "--debug (Enable the Java debugger on port 5005, attach to debug JavaScript)\n"
                        "\nMore info about differences in runtime versions: https://getsandbox.com/docs/js-libraries"
        );
    }


}
