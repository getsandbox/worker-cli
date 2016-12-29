package com.sandbox.runtime.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.runtime.utils.JSONUtils;
import org.junit.Test;

/**
 * Created by nickhoughton on 20/11/2015.
 */
public class JSONUtilsTest {
    @Test
    public void testEncodedObj() throws Exception {
        JSONUtils.parse(new ObjectMapper(), "\"{\\n  \\\"users\\\" : [ ]\\n}\"");
    }

    @Test
    public void testEncodedArray() throws Exception {
        JSONUtils.parse(new ObjectMapper(), "\"[{\\n  \\\"users\\\" : [ ]\\n}]\"");
    }

    @Test
    public void testObj() throws Exception {
        JSONUtils.parse(new ObjectMapper(), "{}");
    }

    @Test
    public void testArray() throws Exception {
        JSONUtils.parse(new ObjectMapper(), "[{}]");
    }
}
