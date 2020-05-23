package com.sandbox.worker.core.utils;

import com.sandbox.worker.models.Error;
import com.sandbox.worker.core.exceptions.ServiceScriptException;

import java.util.regex.Pattern;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.SourceSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorUtils.class);

    public static ServiceScriptException getServiceScriptException(Exception exception) {
        Throwable cause = unwrapThrowable(exception);

        if (cause instanceof PolyglotException) {
            SourceSection sourceSection = ((PolyglotException) cause).getSourceLocation();
            if (sourceSection != null) {
                return new ServiceScriptException((Exception) cause, sourceSection.getSource().getName(), sourceSection.getStartLine(), sourceSection.getStartColumn());
            } else {
                return new ServiceScriptException(cause.getMessage(), cause);
            }

        } else if (cause instanceof RuntimeException) {
            LOG.error("There was a problem handling your request. Please try again in a minute", cause);
            return new ServiceScriptException("There was a problem handling your request. Please try again in a minute", cause);

        } else if (cause instanceof StackOverflowError) {
            LOG.error("Request has exceeded execution limits", cause);
            return new ServiceScriptException("Request has exceeded execution limits");

        } else {
            LOG.error("Unknown error processing request", cause);
            return new ServiceScriptException("We encountered a system error. Please try again shortly");
        }
    }

    public static Error extractError(Throwable e) {
        return extractError(e, null);
    }

    public static Error extractError(Throwable e, String repositoryBasePath) {
        Error error = new Error();

        if (e instanceof ServiceScriptException) {
            // e.getMessage should return the details of the js error with line numbers etc.
            error.setDisplayMessage("Runtime failure");

            //if other exception fields are set, then we need to use them otherwise detail is already in the message
            error.setDetailedMessage(e.getMessage());

        } else if (e instanceof RuntimeException) {
            if (e.getCause() != null) {
                return extractError(e.getCause(), repositoryBasePath);
            } else {
                error.setDisplayMessage("There was a problem handling your request. Please try again in a minute");
            }

        } else {
            error.setDisplayMessage(e.getMessage());
        }

        String functionWrapper = "(function (exports, require, module, __filename, __dirname) { ";
        String evalCaret = " in <eval> at line number";

        if (repositoryBasePath != null && error.getDetailedMessage() != null && error.getDetailedMessage().indexOf(repositoryBasePath) != -1) {
            error.setDetailedMessage(error.getDetailedMessage().replaceAll(Pattern.quote(repositoryBasePath), ""));
        }

        if (error.getDetailedMessage() != null && error.getDetailedMessage().indexOf(functionWrapper) != -1) {
            error.setDetailedMessage(error.getDetailedMessage().replaceAll(Pattern.quote(functionWrapper), ""));
        }

        if (error.getDetailedMessage() != null && error.getDetailedMessage().indexOf(evalCaret) != -1) {
            error.setDetailedMessage(error.getDetailedMessage().substring(0, error.getDetailedMessage().indexOf(evalCaret)));
        }

        return error;
    }

    public static Throwable unwrapThrowable(Throwable exception) {
        while (exception.getCause() != null && exception.getCause() instanceof Throwable) {
            if (exception == exception.getCause()) {
                return exception;
            }
            exception = exception.getCause();
        }
        return exception;
    }
}
