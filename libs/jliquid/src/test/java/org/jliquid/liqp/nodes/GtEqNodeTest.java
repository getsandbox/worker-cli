package org.jliquid.liqp.nodes;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GtEqNodeTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{% if nil >= 42.09 %}yes{% else %}no{% endif %}", "no"},
            {"{% if 42.1 >= false %}yes{% else %}no{% endif %}", "no"},
            {"{% if 42.1 >= true %}yes{% else %}no{% endif %}", "no"},
            {"{% if a >= 42.09 %}yes{% else %}no{% endif %}", "no"},
            {"{% if 42.1 >= 42.09 %}yes{% else %}no{% endif %}", "yes"},
            {"{% if 42.1 >= 42.1000001 %}yes{% else %}no{% endif %}", "no"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }
}
