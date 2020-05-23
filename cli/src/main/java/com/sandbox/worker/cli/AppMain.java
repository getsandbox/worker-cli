package com.sandbox.worker.cli;

import com.sandbox.worker.cli.config.Config;
import io.micronaut.runtime.Micronaut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppMain {

    private static final Logger LOG = LoggerFactory.getLogger(AppMain.class);

    public void start(String[] args, Config config) {
        Micronaut.build(args).singletons(config).start();
    }

}
