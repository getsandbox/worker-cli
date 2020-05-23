package org.jliquid.liqp.tags;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CycleTest {

    @Test
    public void applyTest() throws RecognitionException {

        String[][] tests = {
            {
                "{% cycle 'o', 't' %}\n"
                + "{% cycle 33: 'one', 'two', 'three' %}\n"
                + "{% cycle 33: 'one', 'two', 'three' %}\n"
                + "{% cycle 3: '1', '2' %}\n"
                + "{% cycle 33: 'one', 'two' %}\n"
                + "{% cycle 33: 'one', 'two' %}\n"
                + "{% cycle 3: '1', '2' %}\n"
                + "{% cycle 3: '1', '2' %}\n"
                + "{% cycle 'o', 't' %}\n"
                + "{% cycle 'o', 't' %}",
                "o\n"
                + "one\n"
                + "two\n"
                + "1\n"
                + "\n"
                + "one\n"
                + "2\n"
                + "1\n"
                + "t\n"
                + "o"
            },
            {
                "{% cycle 'o', 'p' %}\n"
                + "{% cycle 'o' %}\n"
                + "{% cycle 'o' %}\n"
                + "{% cycle 'o', 'p' %}\n"
                + "{% cycle 'o', 'p' %}",
                "o\n"
                + "o\n"
                + "o\n"
                + "p\n"
                + "o"
            },
            {
                "{% cycle 'one', 'two', 'three' %}\n"
                + "{% cycle 'one', 'two', 'three' %}\n"
                + "{% cycle 'one', 'two' %}\n"
                + "{% cycle 'one', 'two', 'three' %}\n"
                + "{% cycle 'one' %}",
                "one\n"
                + "two\n"
                + "one\n"
                + "three\n"
                + "one"
            }
        };

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_cycle
     *   assert_template_result('one','{%cycle "one", "two"%}')
     *   assert_template_result('one two','{%cycle "one", "two"%} {%cycle "one", "two"%}')
     *   assert_template_result(' two','{%cycle "", "two"%} {%cycle "", "two"%}')
     *
     *   assert_template_result('one two one','{%cycle "one", "two"%} {%cycle "one", "two"%} {%cycle "one", "two"%}')
     *
     *   assert_template_result('text-align: left text-align: right',
     *     '{%cycle "text-align: left", "text-align: right" %} {%cycle "text-align: left", "text-align: right"%}')
     * end
     */
    @Test
    public void cycleTest() throws Exception {

        assertThat(Template.parse("{%cycle \"one\", \"two\"%}").render(), is("one"));
        assertThat(Template.parse("{%cycle \"one\", \"two\"%} {%cycle \"one\", \"two\"%}").render(), is("one two"));
        assertThat(Template.parse("{%cycle \"\", \"two\"%} {%cycle \"\", \"two\"%}").render(), is(" two"));

        assertThat(Template.parse("{%cycle \"one\", \"two\"%} {%cycle \"one\", \"two\"%} {%cycle \"one\", \"two\"%}").render(),
                is("one two one"));

        assertThat(Template.parse("{%cycle \"text-align: left\", \"text-align: right\" %} {%cycle \"text-align: left\", \"text-align: right\"%}").render(),
                is("text-align: left text-align: right"));
    }

    /*
     * def test_multiple_cycles
     *   assert_template_result('1 2 1 1 2 3 1',
     *     '{%cycle 1,2%} {%cycle 1,2%} {%cycle 1,2%} {%cycle 1,2,3%} {%cycle 1,2,3%} {%cycle 1,2,3%} {%cycle 1,2,3%}')
     * end
     */
    @Test
    public void multiple_cyclesTest() throws Exception {

        assertThat(
                Template.parse(
                "{%cycle 1,2%} "
                + "{%cycle 1,2%} "
                + "{%cycle 1,2%} "
                + "{%cycle 1,2,3%} "
                + "{%cycle 1,2,3%} "
                + "{%cycle 1,2,3%} "
                + "{%cycle 1,2,3%}").render(),
                is("1 2 1 1 2 3 1"));
    }

    /*
     * def test_multiple_named_cycles
     *   assert_template_result('one one two two one one',
     *     '{%cycle 1: "one", "two" %} {%cycle 2: "one", "two" %} {%cycle 1: "one", "two" %} {%cycle 2: "one", "two" %} {%cycle 1: "one", "two" %} {%cycle 2: "one", "two" %}')
     * end
     */
    @Test
    public void multiple_named_cyclesTest() throws Exception {

        assertThat(
                Template.parse(
                "{%cycle 1: \"one\", \"two\" %} {%cycle 2: \"one\", \"two\" %} "
                + "{%cycle 1: \"one\", \"two\" %} {%cycle 2: \"one\", \"two\" %} "
                + "{%cycle 1: \"one\", \"two\" %} {%cycle 2: \"one\", \"two\" %}").render(),
                is("one one two two one one"));
    }

    /*
     * def test_multiple_named_cycles_with_names_from_context
     *   assigns = {"var1" => 1, "var2" => 2 }
     *   assert_template_result('one one two two one one',
     *     '{%cycle var1: "one", "two" %} {%cycle var2: "one", "two" %} {%cycle var1: "one", "two" %} {%cycle var2: "one", "two" %} {%cycle var1: "one", "two" %} {%cycle var2: "one", "two" %}', assigns)
     * end
     */
    @Test
    public void multiple_named_cycles_with_names_from_contextTest() throws Exception {

        String assigns = "{\"var1\" : 1, \"var2\" : 2 }";

        assertThat(
                Template.parse(
                "{%cycle var1: \"one\", \"two\" %} {%cycle var2: \"one\", \"two\" %} "
                + "{%cycle var1: \"one\", \"two\" %} {%cycle var2: \"one\", \"two\" %} "
                + "{%cycle var1: \"one\", \"two\" %} {%cycle var2: \"one\", \"two\" %}").render(assigns),
                is("one one two two one one"));
    }
}
