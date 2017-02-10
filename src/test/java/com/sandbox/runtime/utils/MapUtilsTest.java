package com.sandbox.runtime.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MapUtilsTest {

    MapUtils mapUtils = new MapUtils();

    @Test
    public void testSingleValues() throws Exception {
        Map<String, String> values = new HashMap<>();
        values.put("a", "1234");
        values.put("b", "43%7C21");
        values.put("c", "43|21");
        values.put("d", "!@#$^&*()_-=<>?|}{:'\"");
        values.put("e", "auth-%21%40%23%24%5E%26%2A%28%29_-%3D%3C%3E%3F%7C%7D%7B%3A%27%22");
        values.put("f", "value-%^1");
        Map result = mapUtils.flattenMultiValue(values);

        assertEquals("1234", result.get("a"));
        assertEquals("43|21", result.get("b"));
        assertEquals("43|21", result.get("c"));
        assertEquals("!@#$^&*()_-=<>?|}{:'\"", result.get("d"));
        assertEquals("auth-!@#$^&*()_-=<>?|}{:'\"", result.get("e"));
        assertEquals("value-%^1", result.get("f"));

    }

    @Test
    public void testMultiValues() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("a", Arrays.asList("1234", "4321", "abc"));
        values.put("b", new String[]{"1234", "abc", "4321"});
        Map result = mapUtils.flattenMultiValue(values);

        assertEquals("abc", result.get("a"));
        assertEquals("4321", result.get("b"));
    }
}