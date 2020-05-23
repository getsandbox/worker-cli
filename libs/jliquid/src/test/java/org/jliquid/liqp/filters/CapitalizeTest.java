package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CapitalizeTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{{'a' | capitalize}}", "A"},
            {"{{'' | capitalize}}", ""},
            {"{{1 | capitalize}}", "1"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     *
     */
    @Test
    public void applyOriginalTest() {

        Filter filter = Filter.getFilter("capitalize");

        assertThat(filter.apply("testing"), is((Object) "Testing"));
        assertThat(filter.apply(null), is((Object) ""));
    }
}
