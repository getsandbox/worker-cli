package com.sandbox.worker.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.worker.cli.config.Config;
import com.sandbox.worker.cli.services.CLIHttpMessageConverter;
import com.sandbox.worker.core.services.FileBasedActivityStore;
import com.sandbox.worker.core.services.SandboxInMemoryEventService;
import com.sandbox.worker.models.events.SandboxRequestEvent;
import com.sandbox.worker.models.interfaces.ActivityStore;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class BeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(BeanFactory.class);

    @Singleton
    public FileBasedActivityStore activityStore(Config config, ObjectMapper mapper) throws IOException {
        File tempDir = Files.createTempDirectory("activity-store").toFile();
        tempDir.deleteOnExit();

        return new FileBasedActivityStore(tempDir,
                (message, stream) -> {
                    try {
                        mapper.writeValue(stream, message);
                    } catch (IOException e) {
                        LOG.error("Error writing activity to store", e);
                    }
                },
                config.getActivityStorageLimit()
        );
    }

    @Singleton
    public CLIHttpMessageConverter httpMessageConverter(){
        return new CLIHttpMessageConverter();
    }

    @Singleton
    public SandboxInMemoryEventService eventService(ActivityStore activityStore){
        //only include activity messages for listener
        SandboxRequestEvent dummyEvent = new SandboxRequestEvent();
        SandboxInMemoryEventService eventService = new SandboxInMemoryEventService();
        eventService.setMapper((a, t) -> t.equals(SandboxRequestEvent.class.getSimpleName()) ? dummyEvent : null);
        eventService.addEventListener((event) -> {
            ((SandboxRequestEvent) event).getActivityMessages().forEach(activityStore::add);
        });
        return eventService;
    }

}
