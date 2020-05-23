package org.jliquid.liqp.nodes;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OrNodeTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
                {"{% if 42 or 1234 %}TRUE{% else %}FALSE{% endif %}", "TRUE"},
                {"{% if 'x' or true %}TRUE{% else %}FALSE{% endif %}", "TRUE"},
                {"{% if false or true %}TRUE{% else %}FALSE{% endif %}", "TRUE"},
                {"{% if false or '' %}TRUE{% else %}FALSE{% endif %}", "TRUE"},
                {"{% if nil or true %}TRUE{% else %}FALSE{% endif %}", "TRUE"},
                {"{% if nil or false %}TRUE{% else %}FALSE{% endif %}", "FALSE"},
        };

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }
}
