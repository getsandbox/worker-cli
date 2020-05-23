package com.sandbox.worker.core.services;

import com.sandbox.worker.models.interfaces.EventConsumerService;
import com.sandbox.worker.models.interfaces.EventEmitterService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;

//If used as a singleton, an event can be emitted and then confused within the same context/jvm without a queue/more complicated abstraction
public class InMemoryEventService<E> implements EventConsumerService<String, E>, EventEmitterService<E> {
    private static Map<String, InMemoryEventService> topics = new ConcurrentHashMap<>();
    private ExecutorService eventHandler = Executors.newWorkStealingPool();
    private List<Consumer<E>> listeners = new ArrayList<>();
    private boolean started = true;
    private BiFunction<Map<String, String>, String, E> filter;

    public static InMemoryEventService create(String topicName){
        return topics.computeIfAbsent(topicName, s -> new InMemoryEventService());
    }

    //allow consumers to attach themselves and receive events
    public void addEventListener(Consumer<E> listener) {
        listeners.add(listener);
    }

    @Override
    public void setMapper(BiFunction<Map<String, String>, String, E> filter) {
        this.filter = filter;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void startAsync() {
        start();
    }

    @Override
    public void stop() {
        started = false;
    }

    private void handleEvent(E event) {
        if (!started) return;
        if (filter.apply(Collections.emptyMap(), event.getClass().getSimpleName()) == null) return;

        for (Consumer<E> listener : listeners) {
            eventHandler.submit(() -> {
                beforeAccept(event);
                listener.accept(event);
            });
        }
    }

    @Override
    public void emitEvent(E event) {
        handleEvent(event);
    }

}
