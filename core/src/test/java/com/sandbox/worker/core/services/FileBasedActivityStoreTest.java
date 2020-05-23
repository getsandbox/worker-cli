package com.sandbox.worker.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.worker.models.ActivityMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBasedActivityStoreTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldWriteAndReadUptoLimit() throws IOException {
        FileBasedActivityStore store = new FileBasedActivityStore(Files.createTempDirectory("filestore").toFile()
                ,(message, stream) -> {
            try {
                mapper.writeValue(stream, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } ,100
        );

        //add less than limit, should get same back.
        IntStream.range(0,50).forEach((num) -> {
            store.add(new ActivityMessage() {
                @Override
                public String getId() {
                    return num + "";
                }
            });
        });
        assertEquals(50, store.getAll().size());

        //add 100, should get ame 100 back.
        IntStream.range(0,100).forEach((num) -> {
            store.add(new ActivityMessage() {
                @Override
                public String getId() {
                    return num + "";
                }
            });
        });

        assertEquals(100, store.getAll().size());

        //add another 100, still should get limit back
        IntStream.range(100,200).forEach((num) -> {
            store.add(new ActivityMessage() {
                @Override
                public String getId() {
                    return num + "";
                }
            });
        });

        assertEquals(100, store.getAll().size());

    }
}