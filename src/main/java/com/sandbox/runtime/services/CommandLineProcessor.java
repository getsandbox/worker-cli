package com.sandbox.runtime.services;

import com.sandbox.runtime.HttpServer;
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
    boolean persistState;
    boolean verboseLogging;

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
        if(basePathStr == null) {
            basePathStr = System.getProperty("user.dir");
        }
        if (basePathStr.startsWith("~")) {
            basePathStr = System.getProperty("user.home") + basePathStr.substring(1);
        }

        try{
            basePath = Paths.get(basePathStr);
            basePath.toRealPath();
        }catch(Exception e){
            throw new RuntimeException("Invalid base path");
        }

        httpPort = environment.getProperty("port",Integer.class, 8080);
        debugPort = 5005;//environment.getProperty("debug",Integer.class, 5005);
        verboseLogging = environment.getProperty("verbose",String.class) == null ? false : true;
    }

    private void showValidArguments(){
        logger.info("No valid argument specified.\n\n" +
                "Commands:\n" +
                "run\t\t Starts a sandbox runtime in the current working directory.\n" +
                "\n" +
                "Options:\n" +
                "--port=<port number>\n" +
                "--base=<base directory> (Overrides working directory)\n" +
                "--verbose (Increases logging verbosity, full request and response bodies etc)\n" //+
//                "--debug (Enable the Java debugger on port 5005, attach to debug JavaScript)\n"
        );
    }

    public Path getBasePath() {
        return basePath;
    }

    public int getHttpPort() {
        return httpPort;
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
}
