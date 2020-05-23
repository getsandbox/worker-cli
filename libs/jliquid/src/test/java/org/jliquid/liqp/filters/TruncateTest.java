package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TruncateTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{ \"txt\" : \"012345678901234567890123456789012345678901234567890123456789\" }";

        String[][] tests = {
            {"{{ nil | truncate }}", ""},
            {"{{ txt | truncate }}", "01234567890123456789012345678901234567890123456..."},
            {"{{ txt | truncate: 5 }}", "01..."},
            {"{{ txt | truncate: 5, '???' }}", "01???"},
            {"{{ txt | truncate: 500, '???' }}", "012345678901234567890123456789012345678901234567890123456789"},
            {"{{ txt | truncate: 2, '===' }}", "==="},
            {"{{ '12345' | truncate: 4, '===' }}", "1==="}
        };

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_truncate
     *   assert_equal '1234...', @filters.truncate('1234567890', 7)
     *   assert_equal '1234567890', @filters.truncate('1234567890', 20)
     *   assert_equal '...', @filters.truncate('1234567890', 0)
     *   assert_equal '1234567890', @filters.truncate('1234567890')
     * end
     */
    @Test
    public void applyOriginalTest() {

        final Filter filter = Filter.getFilter("truncate");

        assertThat(filter.apply("1234567890", 7), is((Object) "1234..."));
        assertThat(filter.apply("1234567890", 20), is((Object) "1234567890"));
        assertThat(filter.apply("1234567890", 0), is((Object) "..."));
        assertThat(filter.apply("1234567890"), is((Object) "1234567890"));
    }
}
