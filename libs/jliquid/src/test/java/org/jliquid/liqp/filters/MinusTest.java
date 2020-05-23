package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MinusTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{{ 8 | minus: 2 }}", "6"},
            {"{{ 8 | minus: 3 }}", "5"},
            {"{{ 8 | minus: 3. }}", "5.0"},
            {"{{ 8 | minus: 3.0 }}", "5.0"},
            {"{{ 8 | minus: 2.0 }}", "6.0"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    @Test(expected = RuntimeException.class)
    public void applyTestInvalid1() {
        Filter.getFilter("minus").apply(1);
    }

    @Test(expected = RuntimeException.class)
    public void applyTestInvalid2() {
        Filter.getFilter("minus").apply(1, 2, 3);
    }

    /*
     * def test_minus
     *   assert_template_result "4", "{{ input | minus:operand }}", 'input' => 5, 'operand' => 1
     *   assert_template_result "2.3", "{{ '4.3' | minus:'2' }}"
     * end
     */
    @Test
    public void applyOriginalTest() {

        assertThat(Template.parse("{{ input | minus:operand }}").render("{\"input\":5, \"operand\":1}"), is((Object) "4"));
        assertThat(Template.parse("{{ '4.3' | minus:'2' }}").render(), is((Object) "2.3"));
    }
}
