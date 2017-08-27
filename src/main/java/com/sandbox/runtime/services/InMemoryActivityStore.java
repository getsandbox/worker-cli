package com.sandbox.runtime.services;

import com.sandbox.runtime.models.ActivityMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class InMemoryActivityStore {

    private int limit;

    private Collection<ActivityMessage> messages;

    public InMemoryActivityStore(int limit) {
        this.limit = limit;
        this.messages = Collections.synchronizedCollection(new CircularFifoQueue<ActivityMessage>(limit));
    }

    public void add(ActivityMessage message)  {
        messages.add(message);
    }

    public List<ActivityMessage> getAll(){
        return new ArrayList<>(messages);
    }

}
