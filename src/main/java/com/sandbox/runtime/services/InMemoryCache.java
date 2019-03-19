package com.sandbox.runtime.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sandbox.runtime.js.services.ServiceManager;
import com.sandbox.runtime.models.MetadataService;
import com.sandbox.runtime.models.RepositoryService;
import com.sandbox.runtime.models.RoutingTable;
import com.sandbox.runtime.models.RoutingTableCache;
import com.sandbox.runtime.models.StateService;
import com.sandbox.runtime.models.config.RuntimeConfig;
import com.sun.nio.file.SensitivityWatchEventModifier;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by nickhoughton on 18/10/2014.
 */
public class InMemoryCache implements RepositoryService, MetadataService, RoutingTableCache, StateService {

    @Autowired
    Environment environment;

    @Autowired
    RuntimeConfig config;

    @Autowired
    ServiceManager serviceManager;

    @Autowired
    ObjectMapper mapper;

    RoutingTable routingTable;

    private HashMap<String, String> fileContents = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(InMemoryCache.class);

    @PostConstruct
    void init(){
        if(config.isEnableFileWatch()){
            listenForFileChange(config.getBasePath());
        }
    }

    @Override
    public String getRepositoryFile(String fullSandboxId, String filename) {
        if(fileContents.containsKey(filename)){
           return fileContents.get(filename);
        }
        if(Files.exists(config.getBasePath().resolve(filename))){
            try {
                String result = FileUtils.readFileToString(config.getBasePath().resolve(filename).toFile());
                fileContents.put(filename, result);
                return result;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean hasRepositoryFile(String fullSandboxId, String filename) {
        return (Files.exists(config.getBasePath().resolve(filename)));
    }

    @Override
    public String getSandboxState(String sandboxId) {
        String state = "{}";
        //load state if it exists and is correct
        if(config.getStatePath() != null){
            Path stateFilePath = config.getStatePath();
            if(!Files.exists(stateFilePath)){
                logger.warn("State path has been specified, but the '{}' file does not exist, creating..", stateFilePath);
                setSandboxState(sandboxId, state);
            }else {
                try {
                    logger.info("Loading state from '{}'", stateFilePath);
                    state = FileUtils.readFileToString(stateFilePath.toFile());
                } catch (IOException e) {
                    logger.error("Error reading persisted state, ignoring..", e);
                }
            }
        }
        return state;
    }

    @Override
    public void setSandboxState(String sandboxId, String state) {
        Path stateFilePath = config.getStatePath();
        try {
            FileUtils.writeStringToFile(stateFilePath.toFile(), state, "UTF-8");
        } catch (IOException e) {
            logger.error("Error writing state to file '{}'", stateFilePath.toFile().getAbsolutePath());
        }
    }


    @Override
    public void setRoutingTableForSandboxId(String sandboxId, String fullSandboxId, RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    @Override
    public RoutingTable getRoutingTableForSandboxId(String sandboxId, String fullSandboxId) {
        return routingTable;
    }

    @Override
    public Map<String, String> getConfig(String sandboxId) throws Exception {
        //TODO: Allow config to be set somehow, prolly JVM args?
        HashMap config = new HashMap();
        config.put("sandbox_runtime_version", this.config.getRuntimeVersion().toString());
        return config;
    }

    @Override
    public ObjectNode getSandbox(String sandboxId) throws Exception {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ObjectNode getSandboxForSandboxName(String sandboxName) throws Exception {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setSandbox(String sandboxId, String sandboxName, Object sandbox) throws Exception {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void clear(String sandboxId, String sandboxName) {
        //noop
    }

    private void listenForFileChange(Path base){
        try {
            final WatchService watcher = FileSystems.getDefault().newWatchService();
            final SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
                {
                    dir.register(watcher, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
                    return FileVisitResult.CONTINUE;
                }
            };

            Files.walkFileTree(base, fileVisitor);
            new Thread(()->{
                try {
                    WatchKey key = watcher.take();
                    while(key != null) {
                        //if js file has changed, clear routing table
                        for (WatchEvent event : key.pollEvents()) {
                            if(event.context().toString().endsWith(".js")){
                                setRoutingTableForSandboxId("1","1", null);
                                fileContents.clear();
                                serviceManager.refreshAllServices();
                                logger.info("Clearing routing table on JS file change");
                            }
                        }
                        //cleanup
                        key.reset();
                        key = watcher.take();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
