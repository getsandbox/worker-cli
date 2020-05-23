package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TimesTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{{ 8 | times: 2 }}", "16"},
            {"{{ 8 | times: 3 }}", "24"},
            {"{{ 8 | times: 3. }}", "24.0"},
            {"{{ 8 | times: '3.0' }}", "24.0"},
            {"{{ 8 | times: 2.0 }}", "16.0"},
            {"{{ foo | times: 4 }}", "0"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    @Test(expected = RuntimeException.class)
    public void applyTestInvalid1() {
        Filter.getFilter("times").apply(1);
    }

    @Test(expected = RuntimeException.class)
    public void applyTestInvalid2() {
        Filter.getFilter("times").apply(1, 2, 3);
    }

    /*
     * def test_times
     *   assert_template_result "12", "{{ 3 | times:4 }}"
     *   assert_template_result "0", "{{ 'foo' | times:4 }}"
     *
     *   # Ruby v1.9.2-rc1, or higher, backwards compatible Float test
     *   assert_match(/(6\.3)|(6\.(0{13})1)/, Template.parse("{{ '2.1' | times:3 }}").render)
     *
     *   assert_template_result "6", "{{ '2.1' | times:3 | replace: '.','-' | plus:0}}"
     * end
     */
    @Test
    public void applyOriginalTest() {

        Filter filter = Filter.getFilter("times");

        assertThat(filter.apply(3L, 4L), is((Object) 12L));
        // assert_template_result "0", "{{ 'foo' | times:4 }}" // see: applyTest()
        assertTrue(String.valueOf(filter.apply(2.1, 3L)).matches("6[.,]30{10,}1"));
    }
}
