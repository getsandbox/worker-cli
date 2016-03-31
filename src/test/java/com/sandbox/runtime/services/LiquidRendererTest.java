package com.sandbox.runtime.services;

import com.sandbox.runtime.js.utils.NashornUtils;
import org.jliquid.liqp.LimitedStringBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by nickhoughton on 4/08/2014.
 */
public class LiquidRendererTest {
    @Test
    public void testSimple() throws Exception {

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("name","ando");
        String result = new LiquidRenderer().render("hi {{name}}", vars);
        assertEquals("hi ando",result);
    }

    @Test
    public void testPropertiesWithDots() throws Exception {

        Map<String, Object> vars = new HashMap<String, Object>();
        Map<String, Object> form = new HashMap<String, Object>();
        List list = Arrays.asList(form);
        form.put("a.b","ando");
        vars.put("list", list);

        String result = new LiquidRenderer().render("hi {{list[0][\"a.b\"]}}", vars);
        assertEquals("hi ando",result);
    }

    @Test
    public void testPropertiesWithForLoop() throws Exception {
        Map<String, Object> inner = new HashMap<String, Object>();
        inner.put("blah","meep");


        Map<String, Object> outer = new HashMap<String, Object>();
        outer.put("a.b", inner);

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("hash", outer);

        String result = new LiquidRenderer().render("{% for item in hash %}\n{{item[0]}}: {{ item[1].blah }}\n{% endfor %}", vars);
        assertEquals("a.b: meep", result);

        result = new LiquidRenderer().render("{% for item in hash %}stuff!\n{{item[0]}}: {{ item[1].blah }}\nonnewline{% endfor %}", vars);
        assertEquals("stuff!\n" +
                "a.b: meep\n" +
                "onnewline",result);
    }

    @Test
    public void testIncludeWithTemplate() throws Exception {

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("name", "ando");
        vars.put("__nashornUtils", new NashornUtils() {
            @Override
            public String jsonStringify(Object o) {
                return null;
            }

            @Override
            public String readFile(String filename) {
                if(filename.equals("templates/blah.liquid")){
                    return "hi {{name}}";
                }else{
                    return null;
                }
            }

            @Override
            public boolean hasFile(String filename) {
                return false;
            }
        });
        String result = new LiquidRenderer().render("{% include 'blah' %}", vars);
        assertEquals("hi ando", result);

    }

    @Test
    public void testIncludeWithTemplateSlug() throws Exception {

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("name", "ando");
        vars.put("__nashornUtils", new NashornUtils() {
            @Override
            public String jsonStringify(Object o) {
                return null;
            }

            @Override
            public String readFile(String filename) {
                if(filename.equals("templates/blah.liquid")){
                    return "hi {{name}}";
                }else{
                    return null;
                }
            }

            @Override
            public boolean hasFile(String filename) {
                return false;
            }
        });
        String result = new LiquidRenderer().render("{% include 'blah' with 'smth' %}", vars);
        assertEquals("hi ando", result);

    }

    @Test(expected = RuntimeException.class)
    public void testIncludeWithTemplateError() throws Exception {

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("name","ando");
        vars.put("__nashornUtils", new NashornUtils() {
            @Override
            public String jsonStringify(Object o) {
                return null;
            }

            @Override
            public String readFile(String filename) {
                return null;
            }

            @Override
            public boolean hasFile(String filename) {
                return false;
            }
        });
        String result = new LiquidRenderer().render("{% include 'blah' %}", vars);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceededForLoop() throws Exception {
        LimitedStringBuilder.limitInBytes = 10000;
        Map<String, Object> outer = new HashMap<String, Object>();
        for (int x=0;x < 1000; x++){
            outer.put(x+"", "sdl,fjksldjkdfsjkldsflkjsfdjlksfdjlksdfjlksdfjlksdfjlksldfjkjlkfsdsdflsdfjlksdfjlkdsfljksdfjlfkdssdflkjsdflksjdflksjdflkjsflwiejfopqijfqlskdjqlsidjqwldjqwd");
        }

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("hash", outer);

        String result = new LiquidRenderer().render("{% for item in hash %}\n{{ item[1] }}\n{% endfor %}", vars);
    }

}
