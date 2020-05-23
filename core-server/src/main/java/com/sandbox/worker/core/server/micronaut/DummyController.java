package com.sandbox.worker.core.server.micronaut;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;

/*
This is here b/c https://github.com/micronaut-projects/micronaut-core/issues/1113
Micronaut tries to lazy process the body, if you don't use it it doesn't provide it which seems a bit short sighted but hey
So we create a catch all controller with a @Body so it triggers the body processing. We can't actually let a request get passed here though
as you can't it would only work for requests with bodies, doesn't work for GETs with no body. Lucky for us the Filter does all the work anyway so
requests never get passed to this controller.
 */
@Controller
public class DummyController {

    @Error(global = true, status = HttpStatus.NOT_FOUND)
    public HttpResponse<?> handleError(HttpRequest<?> request, @Body String body) {
        return null;
    }

}
