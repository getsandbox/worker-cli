package com.sandbox.runtime.services;

import com.sandbox.runtime.models.config.LatencyStrategyEnum;
import com.sandbox.runtime.models.config.RouteConfig;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nickhoughton on 25/01/2017.
 */
public class RouteConfigUtilsTest {

    @Test
    public void testConstantCalculate() throws Exception {
        StopWatch timer = mock(StopWatch.class);
        when(timer.getTotalTimeMillis()).thenReturn(1000L);

        RouteConfig routeConfig = new RouteConfig();
        routeConfig.setLatencyMs(10000);
        routeConfig.setLatencyType(LatencyStrategyEnum.CONSTANT);

        assertEquals(9000, RouteConfigUtils.calculate(routeConfig, timer, new AtomicInteger(1)));
        assertEquals(9000, RouteConfigUtils.calculate(routeConfig, timer, new AtomicInteger(10)));

    }

    @Test
    public void testLinearCalculate() throws Exception {
        StopWatch timer = mock(StopWatch.class);
        when(timer.getTotalTimeMillis()).thenReturn(0L);

        RouteConfig routeConfig = new RouteConfig();
        routeConfig.setLatencyMs(10000);
        routeConfig.setLatencyType(LatencyStrategyEnum.LINEAR);
        routeConfig.setLatencyMultiplier(1);

        assertEquals(10000, RouteConfigUtils.calculate(routeConfig, timer, new AtomicInteger(1)));
        assertEquals(20000, RouteConfigUtils.calculate(routeConfig, timer, new AtomicInteger(2)));

    }
}