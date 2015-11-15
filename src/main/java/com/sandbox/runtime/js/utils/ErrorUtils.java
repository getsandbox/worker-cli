package com.sandbox.runtime.js.utils;

import com.sandbox.common.models.ServiceScriptException;
import com.sandbox.common.models.Error;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by nickhoughton on 20/10/2014.
 */
@Component
public class ErrorUtils {

    public Error extractError(Throwable e) {
        Error error = new Error();

        if (e instanceof ServiceScriptException) {
            // e.getMessage should return the details of the js error with line numbers etc.
            ServiceScriptException sse = (ServiceScriptException) e;
            error.setDisplayMessage("Runtime failure");

            //if other exception fields are set, then we need to use them otherwise detail is already in the message
            error.setDetailedMessage(e.getMessage());

        } else if (e instanceof RuntimeException) {
            if (e.getCause() != null) {
                return extractError(e.getCause());
            } else {
                error.setDisplayMessage("There was a problem handling your request. Please try again in a minute");
            }

        } else {
            error.setDisplayMessage(e.getMessage());
        }

        String functionWrapper = "(function (exports, require, module, __filename, __dirname) { ";
        String evalCaret = " in <eval> at line number";

        if (error.getDetailedMessage() != null && error.getDetailedMessage().indexOf(functionWrapper) != -1) {
            error.setDetailedMessage(StringUtils.delete(error.getDetailedMessage(), functionWrapper));
        }

        if (error.getDetailedMessage() != null && error.getDetailedMessage().indexOf(evalCaret) != -1) {
            error.setDetailedMessage(error.getDetailedMessage().substring(0, error.getDetailedMessage().indexOf(evalCaret)));
        }

        return error;
    }
}
