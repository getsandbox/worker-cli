package com.sandbox.runtime.config;

import com.sandbox.runtime.models.RuntimeVersion;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by nickhoughton on 4/10/2016.
 */
public class Config {

    Path basePath = Paths.get("./");
    int httpPort = -1;
    Integer debugPort;
    Path statePath;
    RuntimeVersion runtimeVersion = RuntimeVersion.getLatest();
    boolean verboseLogging = false;
    boolean disableLogging = false;
    boolean disableIDs = false;
    boolean enableConcurrency = false;
    boolean disableRefresh = false;

    public void validate(){
        if(basePath == null){
            throw new IllegalArgumentException("Missing argument: basePath");
        };
        if(!basePath.toFile().exists()){
            throw new IllegalArgumentException("Base path specified doesn't exist");
        };
        if(httpPort < 0){
            throw new IllegalArgumentException("Missing argument: httpPort");
        };
        if(runtimeVersion == null){
            throw new IllegalArgumentException("Missing argument: runtimeVersion");
        };
    }

    public Path getBasePath() {
        return basePath;
    }

    public void setBasePath(Path basePath) {
        try{
            String basePathStr = basePath.toString();
            if(basePathStr == null) basePathStr = System.getProperty("user.dir");
            if(basePathStr.startsWith("~")) basePathStr = System.getProperty("user.home") + basePathStr.substring(1);
            this.basePath = Paths.get(basePathStr);
            //ensure the path actually exists
            this.basePath.toRealPath();
        }catch(Exception e){
            throw new IllegalArgumentException("Invalid base path");
        }
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public Integer getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(Integer debugPort) {
        this.debugPort = debugPort;
    }

    public Path getStatePath() {
        return statePath;
    }

    public void setStatePath(Path statePath) {
        String statePathStr = statePath.toString();
        if(statePathStr != null) {
            try {
                if (statePathStr.startsWith("~"))
                    statePathStr = System.getProperty("user.home") + statePathStr.substring(1);
                this.statePath = Paths.get(statePathStr);
                //ensure the path actually exists
                this.statePath.toRealPath();
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid state path");
            }
        }
    }

    public RuntimeVersion getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setRuntimeVersion(RuntimeVersion runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public void setVerboseLogging(boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
    }

    public boolean isDisableLogging() {
        return disableLogging;
    }

    public void setDisableLogging(boolean disableLogging) {
        this.disableLogging = disableLogging;
    }

    public boolean isDisableIDs() {
        return disableIDs;
    }

    public void setDisableIDs(boolean disableIDs) {
        this.disableIDs = disableIDs;
    }

    public boolean isEnableConcurrency() {
        return enableConcurrency;
    }

    public void setEnableConcurrency(boolean enableConcurrency) {
        this.enableConcurrency = enableConcurrency;
    }

    public boolean isDisableRefresh() {
        return disableRefresh;
    }

    public void setDisableRefresh(boolean disableRefresh) {
        this.disableRefresh = disableRefresh;
    }
}
