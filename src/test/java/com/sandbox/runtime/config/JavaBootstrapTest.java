package com.sandbox.runtime.config;

import com.sandbox.runtime.models.config.RouteConfig;
import com.sandbox.runtime.models.config.RuntimeConfig;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class JavaBootstrapTest {

    @Test
    public void testStart() throws Exception {
        RuntimeConfig runtimeConfig = new RuntimeConfig();

        RouteConfig routeConfig = new RouteConfig();
        routeConfig.setMethod("GET");
        routeConfig.setPath("/hello");
        runtimeConfig.getRoutes().add(routeConfig);
        runtimeConfig.validate();

        JavaBootstrap javaBootstrap = JavaBootstrap.start(runtimeConfig);
        Thread.sleep(5000);
        javaBootstrap.stopInstance();

    }
}
