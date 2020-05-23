package org.jliquid.liqp;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TemplateTest {

    @Test
    public void renderJSONStringTest() throws RecognitionException {

        final String expected = "Hey";

        String rendered = Template.parse("{{mu}}").render("{\"mu\" : \"" + expected + "\"}");
        assertThat(rendered, is(expected));
    }

    @Test(expected = RuntimeException.class)
    public void renderJSONStringTestInvalidJSON() throws RecognitionException {
        Template.parse("mu").render("{\"key : \"value\"}"); // missing quote after `key`
    }

    @Test
    public void renderVarArgsTest() throws RecognitionException {

        final String expected = "Hey";

        String rendered = Template.parse("{{mu}}").render("mu", expected);
        assertThat(rendered, is(expected));

        rendered = Template.parse("{{a}}{{b}}{{c}}").render(
                "a", expected,
                "b", expected,
                "c", null);
        assertThat(rendered, is(expected + expected));

        rendered = Template.parse("{{a}}{{b}}{{c}}").render(
                "a", expected,
                "b", expected,
                "c" /* no value */);
        assertThat(rendered, is(expected + expected));
    }

    @Test(expected = RuntimeException.class)
    public void renderVarArgsTestInvalidKey1() throws RecognitionException {
        Template.parse("mu").render(123, 456);
    }

    @Test(expected = NullPointerException.class)
    public void renderVarArgsTestInvalidKey2() throws RecognitionException {
        Template.parse("mu").render(null, 456);
    }
}
