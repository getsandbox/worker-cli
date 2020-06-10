package com.sandbox.worker.models.interfaces;

@FunctionalInterface
public interface BodyParserFunction<T, R> {

    R apply(T o) throws Exception;

}
