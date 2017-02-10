package com.sandbox.runtime.config;

import com.sandbox.runtime.models.config.RouteConfig;
import com.sandbox.runtime.models.config.RuntimeConfig;
import org.junit.Test;

/**
 * Created by nickhoughton on 24/01/2017.
 */
public class JavaBootstrapTest {

    @Test
    public void testStart() throws Exception {
        JavaBootstrap javaBootstrap = new JavaBootstrap();
        RuntimeConfig runtimeConfig = new RuntimeConfig();

        RouteConfig routeConfig = new RouteConfig();
        routeConfig.setMethod("GET");
        routeConfig.setPath("/hello");
        runtimeConfig.getRoutes().add(routeConfig);
        runtimeConfig.validate();

        javaBootstrap.startInstance(runtimeConfig);
        javaBootstrap.stopInstance();

    }
}
