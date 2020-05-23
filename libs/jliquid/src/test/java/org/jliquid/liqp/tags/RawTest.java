package org.jliquid.liqp.tags;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RawTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {"{% raw %}{% endraw %}", ""},
            {"{% raw %}{{a|b}}{% endraw %}", "{{a|b}}"}
        };

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_tag_in_raw
     *   assert_template_result '{% comment %} test {% endcomment %}',
     *                          '{% raw %}{% comment %} test {% endcomment %}{% endraw %}'
     * end
     */
    @Test
    public void tag_in_rawTest() throws RecognitionException {

        assertThat(Template.parse("{% raw %}{% comment %} test {% endcomment %}{% endraw %}").render(),
                is("{% comment %} test {% endcomment %}"));
    }

    /*
     * def test_output_in_raw
     *   assert_template_result '{{ test }}',
     *                          '{% raw %}{{ test }}{% endraw %}'
     * end
     */
    @Test
    public void output_in_rawTest() throws RecognitionException {

        assertThat(Template.parse("{% raw %}{{ test }}{% endraw %}").render(),
                is("{{ test }}"));
    }
}
