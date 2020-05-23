package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{ \"n\" : [1,2,3,4,5] }";

        String[][] tests = {
            {"{{ nil | h }}", ""},
            {"{{ 42 | h }}", "42"},
            {"{{ n | h }}", "12345"},
            {"{{ '<foo>&\"' | h }}", "&lt;foo&gt;&amp;&quot;"},
            {"{{ false | h }}", "false"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_escape
     *   assert_equal '&lt;strong&gt;', @filters.escape('<strong>')
     *   assert_equal '&lt;strong&gt;', @filters.h('<strong>')
     * end
     */
    @Test
    public void applyOriginalTest() {

        Filter filter = Filter.getFilter("h");

        assertThat(filter.apply("<strong>"), is((Object) "&lt;strong&gt;"));
    }
}
