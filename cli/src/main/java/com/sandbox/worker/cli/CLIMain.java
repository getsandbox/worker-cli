package com.sandbox.worker.cli;

import ch.qos.logback.classic.Level;
import com.sandbox.worker.cli.config.Config;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import picocli.CommandLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CLIMain {

    private static final Logger LOG = LoggerFactory.getLogger(CLIMain.class);

    public static void main(String[] args){
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        try {
            System.getProperties().stringPropertyNames().stream().filter(name -> name.startsWith("LOG")).forEach(prop -> {
                final String loggerName = prop.substring("LOG".length());
                final String levelStr = System.getProperty(prop, "");
                final Level level = Level.toLevel(levelStr, null);
                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerName)).setLevel(level);
                LOG.info("Overriding logging level {} to {}", loggerName, levelStr);
            });

        } catch (Exception e) {
            LOG.warn("Problem overriding logging level from system props.", e);
        }

        Config config = null;
        try {
            config = CommandLine.populateCommand(new Config(), args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            CommandLine.usage(new Config(), System.out);
            System.exit(-1);
        }

        //register shutdown behaviour
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down..");
        }));

        //start app
        try {
            new AppMain().start(args, config);
        } catch (Throwable e) {
            LOG.error("Shutting down with exception", e);
        }
    }
}
