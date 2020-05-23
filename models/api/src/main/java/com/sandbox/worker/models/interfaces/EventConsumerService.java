package com.sandbox.worker.models.interfaces;

import com.sandbox.worker.models.events.Event;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.slf4j.MDC;

public interface EventConsumerService<R, E> {

    void addEventListener(Consumer<E> listener);

    void setMapper(BiFunction<Map<String, String>, R, E> filter);

    void start() throws Exception;

    void startAsync();

    void stop();

    default void beforeAccept(E event) {
        if(event instanceof Event) {
            MDC.put("sandboxRequestId", ((Event) event).getRequestId());
        }
    }

}
