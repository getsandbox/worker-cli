package org.jliquid.liqp.nodes;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NEqNodeTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{% if 1.0 != 1 %}TRUE{% else %}FALSE{% endif %}", "FALSE"},
            {"{% if nil != nil %}TRUE{% else %}FALSE{% endif %}", "FALSE"},
            {"{% if false != false %}TRUE{% else %}FALSE{% endif %}", "FALSE"},
            {"{% if \"\" != '' %}TRUE{% else %}FALSE{% endif %}", "FALSE"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }
}
