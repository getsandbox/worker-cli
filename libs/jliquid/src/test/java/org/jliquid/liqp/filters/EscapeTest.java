package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EscapeTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{ \"n\" : [1,2,3,4,5] }";

        String[][] tests = {
            {"{{ nil | escape }}", ""},
            {"{{ 42 | escape }}", "42"},
            {"{{ n | escape }}", "12345"},
            {"{{ '<foo>&\"' | escape }}", "&lt;foo&gt;&amp;&quot;"},
            {"{{ false | escape }}", "false"},};

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

        Filter filter = Filter.getFilter("escape");

        assertThat(filter.apply("<strong>"), is((Object) "&lt;strong&gt;"));
    }
}
