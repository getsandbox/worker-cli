package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Strip_NewlinesTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{ \"a\" : \"1\\r\\r\\n\\n\\r\\n2\\r3\" }";

        String[][] tests = {
            {"{{ nil | strip_newlines }}", ""},
            {"{{ a | strip_newlines }}", "123"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_strip_newlines
     *   assert_template_result 'abc', "{{ source | strip_newlines }}", 'source' => "a\nb\nc"
     * end
     */
    @Test
    public void applyOriginalTest() {

        assertThat(Template.parse("{{ source | strip_newlines }}").render("source", "a\nb\nc"), is((Object) "abc"));
    }
}
