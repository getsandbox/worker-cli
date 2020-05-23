package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PlusTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{{ 8 | plus: 2 }}", "10"},
            {"{{ 8 | plus: 3 }}", "11"},
            {"{{ 8 | plus: '3.' }}", "11.0"},
            {"{{ 8 | plus: 3.0 }}", "11.0"},
            {"{{ 8 | plus: \"2.0\" }}", "10.0"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    @Test(expected = RuntimeException.class)
    public void applyTestInvalid1() {
        Filter.getFilter("plus").apply(1);
    }

    @Test(expected = RuntimeException.class)
    public void applyTestInvalid2() {
        Filter.getFilter("plus").apply(1, 2, 3);
    }

    /*
     * def test_plus
     *   assert_template_result "2", "{{ 1 | plus:1 }}"
     *   assert_template_result "2.0", "{{ '1' | plus:'1.0' }}"
     * end
     */
    @Test
    public void applyOriginalTest() {

        assertThat(Template.parse("{{ 1 | plus:1 }}").render(), is((Object) "2"));
        assertThat(Template.parse("{{ '1' | plus:'1.0' }}").render(), is((Object) "2.0"));
    }
}
