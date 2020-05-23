package com.sandbox.worker.core.services;

import com.sandbox.worker.models.ActivityMessage;
import com.sandbox.worker.models.interfaces.ActivityStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class FileBasedActivityStore implements ActivityStore {

    private AtomicInteger messageIdCounter = new AtomicInteger(0);

    private boolean enabled = false;
    private final int limit;
    private final File storagePath;
    private final BiConsumer<ActivityMessage, OutputStream> serialiser;
    private int index = -1;
    private boolean wrapped = false;

    public FileBasedActivityStore(File storagePath, BiConsumer<ActivityMessage, OutputStream> serialiser, int limit) {
        this.storagePath = storagePath;
        this.storagePath.mkdirs();
        this.limit = limit;
        this.serialiser = serialiser;

        if (limit > 0) {
            this.enabled = true;
        }
    }

    public void add(ActivityMessage message) {
        if (enabled) {
            message.setMessageId(messageIdCounter.getAndIncrement() + "");
            try {
                serialiser.accept(message, new FileOutputStream(getFileForIndex(getLatestIndex())));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    protected File getFileForIndex(int index){
        return storagePath.toPath().resolve("activity-" + index).toFile();
    }

    protected synchronized int getLatestIndex(){
        index += 1;
        if (index == limit){
            wrapped = true;
            index = -1;
        }
        return index;
    }

    public List<File> getAllAsFiles() {
        List messages = new ArrayList(limit);
        int upperBound = wrapped ? limit-1 : index;
        for (int x = 0; x <= upperBound; x++){
            messages.add(getFileForIndex(x));
        }
        return messages;
    }

    public List<InputStream> getAll() {
        List messages = new ArrayList(limit);
        int upperBound = wrapped ? limit-1 : index;
        for (int x = 0; x <= upperBound; x++){
            try {
                messages.add(new FileInputStream(getFileForIndex(x)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    public List<InputStream> getAll(String keyword) {
        return getAll();
    }

}
