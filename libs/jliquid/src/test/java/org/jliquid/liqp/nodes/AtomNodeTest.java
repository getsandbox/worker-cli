package org.jliquid.liqp.nodes;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AtomNodeTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"mu", "mu"},
            {"1", "1"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }
}
