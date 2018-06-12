package com.sandbox.runtime.services;

import com.sandbox.runtime.models.ActivityMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class InMemoryActivityStore {

    private boolean enabled = false;

    private Collection<ActivityMessage> messages;

    public InMemoryActivityStore(int limit) {
        if (limit > 0) {
            this.enabled = true;
            this.messages = Collections.synchronizedCollection(new CircularFifoQueue<ActivityMessage>(limit));
        } else {
            this.messages = Collections.emptyList();
        }
    }

    public void add(ActivityMessage message) {
        if (enabled) {
            messages.add(message);
        }
    }

    public List<ActivityMessage> getAll() {
        return new ArrayList<>(messages);
    }

}
