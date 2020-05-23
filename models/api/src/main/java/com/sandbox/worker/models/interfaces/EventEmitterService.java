package com.sandbox.worker.models.interfaces;

public interface EventEmitterService<E> {

    void emitEvent(E event);

}
