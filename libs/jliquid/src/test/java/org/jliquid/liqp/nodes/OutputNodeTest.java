package org.jliquid.liqp.nodes;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OutputNodeTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{{ X }}", "mu"},
            {"{{ 'a.b.c' | split:'.' | first | upcase }}", "A"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render("{\"X\" : \"mu\"}"));

            assertThat(rendered, is(test[1]));
        }
    }
}
