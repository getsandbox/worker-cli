package com.sandbox.worker.core.server.micronaut;

import io.micronaut.context.annotation.Primary;
import io.micronaut.http.server.netty.FormDataHttpContentSubscriberFactory;
import io.micronaut.http.server.netty.HttpContentProcessor;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;

import javax.inject.Singleton;

@Primary
@Singleton
public class DisabledFormDataHttpContentSubscriberFactory extends FormDataHttpContentSubscriberFactory {


    /**
     * @param configuration The {@link NettyHttpServerConfiguration}
     */
    public DisabledFormDataHttpContentSubscriberFactory(NettyHttpServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    public HttpContentProcessor build(NettyHttpRequest request) {
        //disabled b/c we want the stream for form data too
        return null;
    }
}
