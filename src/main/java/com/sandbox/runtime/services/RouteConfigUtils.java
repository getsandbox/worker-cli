package com.sandbox.runtime.services;

import com.sandbox.runtime.models.config.LatencyStrategyEnum;
import com.sandbox.runtime.models.config.RouteConfig;
import org.springframework.util.StopWatch;

import java.util.concurrent.atomic.AtomicInteger;

public class RouteConfigUtils {

    public static long calculate(RouteConfig routeConfig, StopWatch currentRequestTimer, AtomicInteger concurrentConsumers){
        long calculatedDelay = calculate(routeConfig, concurrentConsumers);
        long adjustedDelay = calculatedDelay - currentRequestTimer.getTotalTimeMillis();
        return adjustedDelay > 0L ? adjustedDelay : 0L;
    }

    public static long calculate(RouteConfig routeConfig, AtomicInteger concurrentConsumers){
        if(routeConfig != null && routeConfig.getLatencyType() != null && routeConfig.getLatencyType() != LatencyStrategyEnum.NONE){

            if(routeConfig.getLatencyType() == LatencyStrategyEnum.CONSTANT){
                //constant latency is same for everything
                return routeConfig.getLatencyMs();
            }else if(routeConfig.getLatencyType() == LatencyStrategyEnum.LINEAR){
                //linear latency is calculated as, # concurrent consumers * (latency ms * latency multiplier)
                return concurrentConsumers.get() * (routeConfig.getLatencyMs() * routeConfig.getLatencyMultiplier());
            }
        }

        return 0L;
    }

}
