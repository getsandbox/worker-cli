package com.sandbox.runtime.services;

import com.sandbox.runtime.models.ActivityMessage;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class InMemoryActivityStore {

    private int limit;

    private CircularFifoQueue<ActivityMessage> messages;

    public InMemoryActivityStore(int limit) {
        this.limit = limit;
        this.messages = new CircularFifoQueue<>(limit);
    }

    public void add(ActivityMessage message)  {
        messages.add(message);
    }

    public List<ActivityMessage> getAll(){
        return messages.stream().collect(Collectors.toList());
    }

}
