package com.sandbox.runtime.services;

import com.sandbox.runtime.HttpServer;
import com.sandbox.runtime.js.models.RuntimeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by nickhoughton on 23/10/2014.
 */
public class CommandLineProcessor {

    @Autowired
    Environment environment;

    @Autowired
    ApplicationContext context;

    //options
    Path basePath;
    int httpPort;
    Integer debugPort;
    Path statePath;
    RuntimeVersion runtimeVersion;
    boolean verboseLogging;
    boolean disableLogging;
    boolean disableIDs;
    boolean enableConcurrency;
    boolean disableRefresh;

    private static Logger logger = LoggerFactory.getLogger(CommandLineProcessor.class);

    @PostConstruct
    public void start(){
        //pull out optional arguments
        extractOptionalArguments();
    }

    public void process(){
        //process command line args to figure out what to start/do
        String[] args = environment.getProperty("nonOptionArgs", String[].class, new String[]{});
        if(args.length == 0){
            showValidArguments();
            System.exit(1);
        }

        if("run".equals(args[0])){
            HttpServer httpServer = context.getBean(HttpServer.class);
            httpServer.start();
        }else{
            showValidArguments();
            System.exit(1);
        }

    }

    private void extractOptionalArguments(){
        //base path options, fallback to current working direct --base=<dir>
        String basePathStr = environment.getProperty("base",String.class);
        try{
            if(basePathStr == null) basePathStr = System.getProperty("user.dir");
            if(basePathStr.startsWith("~")) basePathStr = System.getProperty("user.home") + basePathStr.substring(1);
            basePath = Paths.get(basePathStr);
            //ensure the path actually exists
            basePath.toRealPath();
        }catch(Exception e){
            throw new RuntimeException("Invalid base path");
        }

        //state path, where we will read/persist state data to
        String statePathStr = environment.getProperty("state",String.class);
        if(statePathStr != null) {
            try {
                if (statePathStr.startsWith("~"))
                    statePathStr = System.getProperty("user.home") + statePathStr.substring(1);
                statePath = Paths.get(statePathStr);
                //ensure the path actually exists
                statePath.toRealPath();
            } catch (Exception e) {
                throw new RuntimeException("Invalid state path");
            }
        }

        //set runtime version, needs to be a valid enum
        String runtimeVersionStr = environment.getProperty("runtimeVersion",String.class, RuntimeVersion.getLatest().toString());
        try{
            runtimeVersion = RuntimeVersion.valueOf(runtimeVersionStr);
        }catch (Exception e){
            throw new RuntimeException("Invalid runtime version");
        }

        httpPort = environment.getProperty("port",Integer.class, 8080);
        debugPort = 5005;//environment.getProperty("debug",Integer.class, 5005);
        verboseLogging = environment.getProperty("verbose",String.class) == null ? false : true;
        disableLogging = environment.getProperty("quiet",String.class) == null ? false : true;
        disableIDs = environment.getProperty("disableIDs",String.class) == null ? false : true;
        enableConcurrency = environment.getProperty("enableConcurrency",String.class) == null ? false : true;
        disableRefresh = environment.getProperty("disableRefresh",String.class) == null ? false : true;
    }

    private void showValidArguments(){
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

    public Path getBasePath() {
        return basePath;
    }

    public Path getStatePath() {
        return statePath;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public RuntimeVersion getRuntimeVersion() {
        return runtimeVersion;
    }

    public Integer getDebugPort() {
        return debugPort;
    }

    public boolean isDebuggingEnabled() {
        return (debugPort != null);
    }

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public boolean isDisableIDs() {
        return disableIDs;
    }

    public boolean concurrencyEnabled() {
        return enableConcurrency;
    }

    public boolean refreshDisabled() {
        return disableRefresh;
    }

    public boolean isDisableLogging() {
        return disableLogging;
    }
}
