package com.sandbox.worker.cli.utils;

import com.sandbox.worker.models.Error;
import com.sandbox.worker.core.utils.ErrorUtils;

import java.io.File;
import java.io.IOException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorUtilsTest {

    ErrorUtils errorUtils = new ErrorUtils();
    String workingDirectory = new File("./").getCanonicalFile().getAbsolutePath();

    ErrorUtilsTest() throws IOException {
    }

    @Test
    void testImportError() throws Exception {
        boolean exception = false;
        try {
            Context context = Context.newBuilder("js").build();
            Source source = Source.newBuilder("js", "import { woot } from 'elsewhere.mjs'", "main.mjs").build();
            context.eval(source);

        } catch (PolyglotException e){
            exception = true;
            Error error = errorUtils.extractError(ErrorUtils.getServiceScriptException(e), workingDirectory);
            assertEquals("Error: Operation is not allowed for: elsewhere.mjs", error.getDetailedMessage());
        }
        Assertions.assertEquals(true, exception);
    }

    @Test
    void testSyntaxError() throws Exception {
        boolean exception = false;
        try {
            Context context = Context.newBuilder("js").build();
            Source source = Source.newBuilder("js", "var x = {};\\nprint('hey')`", "main.js").build();
            context.eval(source);

        } catch (PolyglotException e){
            exception = true;
            Error error = errorUtils.extractError(ErrorUtils.getServiceScriptException(e), workingDirectory);
            assertEquals("main.js:1:27 SyntaxError: main.js:1:26 Missing close quote\n" +
                    "var x = {};\\nprint('hey')`\n" +
                    "                          ^\n", error.getDetailedMessage());
        }
        Assertions.assertEquals(true, exception);
    }

    @Test
    void testTypeError() throws Exception {
        boolean exception = false;
        try {
            Context context = Context.newBuilder("js").build();
            Source source = Source.newBuilder("js", "var x = {}; x('hey')", "main.js").build();
            context.eval(source);

        } catch (PolyglotException e){
            exception = true;
            Error error = errorUtils.extractError(ErrorUtils.getServiceScriptException(e), workingDirectory);
            assertEquals("main.js:1:13 TypeError: x is not a function", error.getDetailedMessage());
        }
        Assertions.assertEquals(true, exception);
    }
}