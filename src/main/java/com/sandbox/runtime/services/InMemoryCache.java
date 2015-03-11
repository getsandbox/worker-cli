package com.sandbox.runtime.services;

import com.sandbox.runtime.models.Cache;
import com.sandbox.runtime.models.RoutingTable;
import com.sun.nio.file.SensitivityWatchEventModifier;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

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

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by nickhoughton on 18/10/2014.
 */
public class InMemoryCache implements Cache {

    @Autowired
    Environment environment;

    @Autowired
    CommandLineProcessor commandLine;

    RoutingTable routingTable;

    String state = "{}";

    private static Logger logger = LoggerFactory.getLogger(InMemoryCache.class);

    @PostConstruct
    void init(){
        listenForFileChange(commandLine.getBasePath());
    }

    @Override
    public String getRepositoryFile(String fullSandboxId, String filename) {
        if(Files.exists(commandLine.getBasePath().resolve(filename))){
            try {
                return FileUtils.readFileToString(commandLine.getBasePath().resolve(filename).toFile());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean hasRepositoryFile(String fullSandboxId, String filename) {
        return (Files.exists(commandLine.getBasePath().resolve(filename)));
    }

    @Override
    public String getSandboxState(String sandboxId) {
        return state;
    }

    @Override
    public void setSandboxState(String sandboxId, String state) {
        this.state = state;
    }


    @Override
    public void setRoutingTableForSandboxId(String sandboxId, RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    @Override
    public RoutingTable getRoutingTableForSandboxId(String sandboxId) {
        return routingTable;
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
                                setRoutingTableForSandboxId("1",null);
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
