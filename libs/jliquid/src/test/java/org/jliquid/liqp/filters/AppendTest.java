package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AppendTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{{ 'a' | append: 'b' }}", "ab"},
            {"{{ '' | append: '' }}", ""},
            {"{{ 1 | append: 23 }}", "123"},
            {"{{ nil | append: 'a' }}", "a"},
            {"{{ nil | append: nil }}", ""},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_append
     *   assigns = {'a' => 'bc', 'b' => 'd' }
     *   assert_template_result('bcd',"{{ a | append: 'd'}}",assigns)
     *   assert_template_result('bcd',"{{ a | append: b}}",assigns)
     * end
     */
    @Test
    public void applyOriginalTest() {

        final String assigns = "{\"a\":\"bc\", \"b\":\"d\" }";

        assertThat(Template.parse("{{ a | append: 'd'}}").render(assigns), is("bcd"));
        assertThat(Template.parse("{{ a | append: b}}").render(assigns), is("bcd"));
    }
}
