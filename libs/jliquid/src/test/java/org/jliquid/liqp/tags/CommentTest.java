package org.jliquid.liqp.tags;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CommentTest {

    @Test
    public void renderTest() throws RecognitionException {

        String[][] tests = {
            {"{% comment %}ABC{% endcomment %}", ""},
            {"A{% comment %}B{% endcomment %}C", "AC"}
        };

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render());

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_has_a_block_which_does_nothing
     *   assert_template_result(%|the comment block should be removed  .. right?|,
     *                          %|the comment block should be removed {%comment%} be gone.. {%endcomment%} .. right?|)
     *
     *   assert_template_result('','{%comment%}{%endcomment%}')
     *   assert_template_result('','{%comment%}{% endcomment %}')
     *   assert_template_result('','{% comment %}{%endcomment%}')
     *   assert_template_result('','{% comment %}{% endcomment %}')
     *   assert_template_result('','{%comment%}comment{%endcomment%}')
     *   assert_template_result('','{% comment %}comment{% endcomment %}')
     *
     *   assert_template_result('foobar','foo{%comment%}comment{%endcomment%}bar')
     *   assert_template_result('foobar','foo{% comment %}comment{% endcomment %}bar')
     *   assert_template_result('foobar','foo{%comment%} comment {%endcomment%}bar')
     *   assert_template_result('foobar','foo{% comment %} comment {% endcomment %}bar')
     *
     *   assert_template_result('foo  bar','foo {%comment%} {%endcomment%} bar')
     *   assert_template_result('foo  bar','foo {%comment%}comment{%endcomment%} bar')
     *   assert_template_result('foo  bar','foo {%comment%} comment {%endcomment%} bar')
     *
     *   assert_template_result('foobar','foo{%comment%}
     *                                    {%endcomment%}bar')
     * end
     */
    @Test
    public void has_a_block_which_does_nothingTest() throws RecognitionException {

        assertThat(Template.parse("the comment block should be removed {%comment%} be gone.. {%endcomment%} .. right?").render(),
                is("the comment block should be removed  .. right?"));

        assertThat(Template.parse("{%comment%}{%endcomment%}").render(), is(""));
        assertThat(Template.parse("{%comment%}{% endcomment %}").render(), is(""));
        assertThat(Template.parse("{% comment %}{%endcomment%}").render(), is(""));
        assertThat(Template.parse("{% comment %}{% endcomment %}").render(), is(""));
        assertThat(Template.parse("{%comment%}comment{%endcomment%}").render(), is(""));
        assertThat(Template.parse("{% comment %}comment{% endcomment %}").render(), is(""));

        assertThat(Template.parse("foo{%comment%}comment{%endcomment%}bar").render(), is("foobar"));
        assertThat(Template.parse("foo{% comment %}comment{% endcomment %}bar").render(), is("foobar"));
        assertThat(Template.parse("foo{%comment%} comment {%endcomment%}bar").render(), is("foobar"));
        assertThat(Template.parse("foo{% comment %} comment {% endcomment %}bar").render(), is("foobar"));

        assertThat(Template.parse("foo {%comment%} {%endcomment%} bar").render(), is("foo  bar"));
        assertThat(Template.parse("foo {%comment%}comment{%endcomment%} bar").render(), is("foo  bar"));
        assertThat(Template.parse("foo {%comment%} comment {%endcomment%} bar").render(), is("foo  bar"));

        assertThat(Template.parse("foo{%comment%}\n         {%endcomment%}bar").render(), is("foobar"));
    }
}
