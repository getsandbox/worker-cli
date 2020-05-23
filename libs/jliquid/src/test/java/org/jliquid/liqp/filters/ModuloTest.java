package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ModuloTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{{ 8 | modulo: 2 }}", "0"},
            {"{{ 8 | modulo: 3 }}", "2"},
            {"{{ \"8\" | modulo: 3. }}", "2.0"},
            {"{{ 8 | modulo: 3.0 }}", "2.0"},
            {"{{ 8 | modulo: '2.0' }}", "0.0"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    @Test(expected = RuntimeException.class)
    public void applyTestInvalid1() {
        Filter.getFilter("modulo").apply(1);
    }

    @Test(expected = RuntimeException.class)
    public void applyTestInvalid2() {
        Filter.getFilter("modulo").apply(1, 2, 3);
    }


    /*
     * def test_modulo
     *   assert_template_result "1", "{{ 3 | modulo:2 }}"
     * end
     */
    @Test
    public void applyOriginalTest() {

        assertThat(Template.parse("{{ 3 | modulo:2 }}").render(), is((Object) "1"));
    }
}
