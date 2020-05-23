package com.sandbox.worker.models;

import com.sandbox.worker.models.enums.ErrorStrategyEnum;
import com.sandbox.worker.models.enums.LatencyStrategyEnum;

public class RouteConfig {

    //force delay/latency config
    private LatencyStrategyEnum latencyType = LatencyStrategyEnum.NONE;
    private int latencyMs = 0;
    private int latencyMultiplier = 1;

    //forced error behaviour config
    private ErrorStrategyEnum errorStrategy;

    public LatencyStrategyEnum getLatencyType() {
        return latencyType;
    }

    public void setLatencyType(LatencyStrategyEnum latencyType) {
        this.latencyType = latencyType;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(int latencyMs) {
        this.latencyMs = latencyMs;
    }

    public int getLatencyMultiplier() {
        return latencyMultiplier;
    }

    public void setLatencyMultiplier(int latencyMultiplier) {
        this.latencyMultiplier = latencyMultiplier;
    }

    public ErrorStrategyEnum getErrorStrategy() {
        return errorStrategy;
    }

    public void setErrorStrategy(ErrorStrategyEnum errorStrategy) {
        this.errorStrategy = errorStrategy;
    }

}
