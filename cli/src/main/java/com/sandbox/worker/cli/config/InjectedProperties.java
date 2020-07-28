package com.sandbox.worker.cli.config;

import io.micronaut.core.annotation.Introspected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Properties;

@Introspected
@Singleton
public class InjectedProperties {

    private static final Logger LOG = LoggerFactory.getLogger(InjectedProperties.class);

    private Properties injectedProperties = null;

    public Properties get(){
        if(injectedProperties == null){
            injectedProperties = new Properties();

            try {
                injectedProperties.load(this.getClass().getClassLoader().getResourceAsStream("values.properties"));
            } catch (IOException e) {
                LOG.warn("Error loading 'values.properties'");
            }
        }

        return injectedProperties;
    }
}
