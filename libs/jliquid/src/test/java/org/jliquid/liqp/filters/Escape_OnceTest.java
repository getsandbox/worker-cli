package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Escape_OnceTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{ \"n\" : [1,2,3,4,5] }";

        String[][] tests = {
            {"{{ nil | escape_once }}", ""},
            {"{{ 42 | escape_once }}", "42"},
            {"{{ n | escape_once }}", "12345"},
            {"{{ '<foo>&\"' | escape_once }}", "&lt;foo&gt;&amp;&quot;"},
            {"{{ false | escape_once }}", "false"},
            {"{{ '&&amp;' | escape_once }}", "&amp;&amp;"},};


        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_escape_once
     *   assert_equal '&lt;strong&gt;', @filters.escape_once(@filters.escape('<strong>'))
     * end
     */
    @Test
    public void applyOriginalTest() {

        final Filter filter = Filter.getFilter("escape_once");

        assertThat(filter.apply(Filter.getFilter("escape").apply("<strong>")), is((Object) "&lt;strong&gt;"));

        // the same test:
        assertThat(filter.apply("&lt;strong&gt;"), is((Object) "&lt;strong&gt;"));
    }
}
