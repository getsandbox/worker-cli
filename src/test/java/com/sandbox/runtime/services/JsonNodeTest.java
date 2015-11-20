package com.sandbox.runtime.services;

import com.sandbox.runtime.js.models.JsonNode;
import org.junit.Test;

/**
 * Created by nickhoughton on 20/11/2015.
 */
public class JsonNodeTest {
    @Test
    public void testEncodedObj() throws Exception {
        new JsonNode("\"{\\n  \\\"users\\\" : [ ]\\n}\"");
    }

    @Test
    public void testEncodedArray() throws Exception {
        new JsonNode("\"[{\\n  \\\"users\\\" : [ ]\\n}]\"");
    }

    @Test
    public void testObj() throws Exception {
        new JsonNode("{}");
    }

    @Test
    public void testArray() throws Exception {
        new JsonNode("[{}]");
    }
}
