package com.sandbox.runtime.services;

import com.sandbox.runtime.models.ActivityMessage;
import com.sandbox.runtime.models.ActivityMessageTypeEnum;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<ActivityMessage> getAll(String keyword){
        return messages.stream()
            .filter(m ->
                keyword == null || (
                m.getMessageType() == ActivityMessageTypeEnum.log && m.getMessage().contains(keyword) ||
                m.getMessageType() == ActivityMessageTypeEnum.request && m.getMessageObject().contains(keyword)
            )
        ).collect(Collectors.toList());

    }

}
