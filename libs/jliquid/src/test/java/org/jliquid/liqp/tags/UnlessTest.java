package org.jliquid.liqp.tags;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UnlessTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{\"user\" : {\"name\" : \"tobi\", \"age\" : 42} }";

        String[][] tests = {
            {"{% unless user.name == 'tobi' %}X{% endunless %}", ""},
            {"{% unless user.name == 'bob' %}X{% endunless %}", "X"},
            {"{% unless user.name == 'tobi' %}X{% else %}Y{% endunless %}", "Y"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_unless
     *   assert_template_result('  ',' {% unless true %} this text should not go into the output {% endunless %} ')
     *   assert_template_result('  this text should go into the output  ',
     *                          ' {% unless false %} this text should go into the output {% endunless %} ')
     *   assert_template_result('  you rock ?','{% unless true %} you suck {% endunless %} {% unless false %} you rock {% endunless %}?')
     * end
     */
    @Test
    public void unlessTest() throws RecognitionException {

        assertThat(Template.parse(" {% unless true %} this text should not go into the output {% endunless %} ").render(), is("  "));
        assertThat(Template.parse(" {% unless false %} this text should go into the output {% endunless %} ").render(), is("  this text should go into the output  "));
        assertThat(Template.parse("{% unless true %} you suck {% endunless %} {% unless false %} you rock {% endunless %}?").render(), is("  you rock ?"));
    }

    /*
     * def test_unless_else
     *   assert_template_result(' YES ','{% unless true %} NO {% else %} YES {% endunless %}')
     *   assert_template_result(' YES ','{% unless false %} YES {% else %} NO {% endunless %}')
     *   assert_template_result(' YES ','{% unless "foo" %} NO {% else %} YES {% endunless %}')
     * end
     */
    @Test
    public void unless_elseTest() throws RecognitionException {

        assertThat(Template.parse("{% unless true %} NO {% else %} YES {% endunless %}").render(), is(" YES "));
        assertThat(Template.parse("{% unless false %} YES {% else %} NO {% endunless %}").render(), is(" YES "));
        assertThat(Template.parse("{% unless \"foo\" %} NO {% else %} YES {% endunless %}").render(), is(" YES "));
    }

    /*
     * def test_unless_in_loop
     *   assert_template_result '23', '{% for i in choices %}{% unless i %}{{ forloop.index }}{% endunless %}{% endfor %}', 'choices' => [1, nil, false]
     * end
     */
    @Test
    public void unless_in_loopTest() throws RecognitionException {

        assertThat(
                Template.parse("{% for i in choices %}{% unless i %}{{ forloop.index }}{% endunless %}{% endfor %}")
                .render("{ \"choices\" : [1, null, false] }"),
                is("23"));
    }

    /*
     * def test_unless_else_in_loop
     *   assert_template_result ' TRUE  2  3 ', '{% for i in choices %}{% unless i %} {{ forloop.index }} {% else %} TRUE {% endunless %}{% endfor %}', 'choices' => [1, nil, false]
     * end
     */
    @Test
    public void unless_else_in_loopTest() throws RecognitionException {

        assertThat(
                Template.parse("{% for i in choices %}{% unless i %} {{ forloop.index }} {% else %} TRUE {% endunless %}{% endfor %}")
                .render("{ \"choices\" : [1, null, false] }"),
                is(" TRUE  2  3 "));
    }
}
