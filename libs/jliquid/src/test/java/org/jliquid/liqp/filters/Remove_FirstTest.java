package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Remove_FirstTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{{ '' | remove_first:'a' }}", ""},
            {"{{ nil | remove_first:'a' }}", ""},
            {"{{ 'aabbabc' | remove_first:'ab' }}", "ababc"},
            {"{{ 'ababab' | remove_first:'a' }}", "babab"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    @Test(expected = RuntimeException.class)
    public void applyTestInvalidPattern() throws RecognitionException {
        Template.parse("{{ 'ababab' | remove_first:nil }}").render();
    }

    /*
     * def test_remove
     *   assert_equal '   ', @filters.remove("a a a a", 'a')
     *   assert_equal 'a a a', @filters.remove_first("a a a a", 'a ')
     *   assert_template_result 'a a a', "{{ 'a a a a' | remove_first: 'a ' }}"
     * end
     */
    @Test
    public void applyOriginalTest() {

        Filter filter = Filter.getFilter("remove_first");

        assertThat(filter.apply("a a a a", "a "), is((Object) "a a a"));
        assertThat(Template.parse("{{ 'a a a a' | remove_first: 'a ' }}").render(), is((Object) "a a a"));
    }
}
