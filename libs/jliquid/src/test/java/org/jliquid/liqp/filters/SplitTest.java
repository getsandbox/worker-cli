package org.jliquid.liqp.filters;

import java.util.regex.Pattern;
import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SplitTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{{ 'a-b-c' | split:'-' }}", "abc"},
            {"{{ 'a-b-c' | split:'' }}", "a-b-c"},
            {"{{ 'a-b-c' | split:'?' }}", "a-b-c"},
            {"{{ 'a-b-c' | split:nil }}", "a-b-c"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_strip
     *   assert_equal ['12','34'], @filters.split('12~34', '~')
     *   assert_equal ['A? ',' ,Z'], @filters.split('A? ~ ~ ~ ,Z', '~ ~ ~')
     *   assert_equal ['A?Z'], @filters.split('A?Z', '~')
     *   # Regexp works although Liquid does not support.
     *   assert_equal ['A','Z'], @filters.split('AxZ', /x/)
     * end
     */
    @Test
    public void applyOriginalTest() {

        final Filter filter = Filter.getFilter("split");

        assertThat(filter.apply("12~34", "~"), is((Object) new String[]{"12", "34"}));
        assertThat(filter.apply("A? ~ ~ ~ ,Z", "~ ~ ~"), is((Object) new String[]{"A? ", " ,Z"}));
        assertThat(filter.apply("A?Z", "~"), is((Object) new String[]{"A?Z"}));
        assertThat(filter.apply("AxZ", Pattern.compile("x")), is((Object) new String[]{"A", "Z"}));
    }
}
