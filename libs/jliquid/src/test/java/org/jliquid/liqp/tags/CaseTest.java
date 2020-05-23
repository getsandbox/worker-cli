package org.jliquid.liqp.tags;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CaseTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{\"x\" : 2, \"y\" : null, \"template\" : \"product\" }";

        String[][] tests = {
            {"{% case x %}{% when 2 %}a{% endcase %}", "a"},
            {"{% case x %}{% when 1 %}a{% when 2 %}b{% else %}c{% endcase %}", "b"},
            {"{% case y %}{% when 1 %}a{% when 2 %}b{% else %}c{% endcase %}", "c"},
            {"{% case template %}{% when '1' %}a{% when 'product' %}P{% else %}c{% endcase %}", "P"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_case
     *   assigns = {'condition' => 2 }
     *   assert_template_result(' its 2 ',
     *                          '{% case condition %}{% when 1 %} its 1 {% when 2 %} its 2 {% endcase %}',
     *                          assigns)
     *
     *   assigns = {'condition' => 1 }
     *   assert_template_result(' its 1 ',
     *                          '{% case condition %}{% when 1 %} its 1 {% when 2 %} its 2 {% endcase %}',
     *                          assigns)
     *
     *   assigns = {'condition' => 3 }
     *   assert_template_result('',
     *                          '{% case condition %}{% when 1 %} its 1 {% when 2 %} its 2 {% endcase %}',
     *                          assigns)
     *
     *   assigns = {'condition' => "string here" }
     *   assert_template_result(' hit ',
     *                          '{% case condition %}{% when "string here" %} hit {% endcase %}',
     *                          assigns)
     *
     *   assigns = {'condition' => "bad string here" }
     *   assert_template_result('',
     *                          '{% case condition %}{% when "string here" %} hit {% endcase %}',
     *                          assigns)
     * end
     */
    @Test
    public void caseTest() throws RecognitionException {

        assertThat(
                Template.parse("{% case condition %}{% when 1 %} its 1 {% when 2 %} its 2 {% endcase %}")
                .render("{ \"condition\":2 }"),
                is(" its 2 "));

        assertThat(
                Template.parse("{% case condition %}{% when 1 %} its 1 {% when 2 %} its 2 {% endcase %}")
                .render("{ \"condition\":1 }"),
                is(" its 1 "));

        assertThat(
                Template.parse("{% case condition %}{% when 1 %} its 1 {% when 2 %} its 2 {% endcase %}")
                .render("{ \"condition\":3 }"),
                is(""));

        assertThat(
                Template.parse("{% case condition %}{% when \"string here\" %} hit {% endcase %}")
                .render("{ \"condition\":\"string here\" }"),
                is(" hit "));

        assertThat(
                Template.parse("{% case condition %}{% when \"string here\" %} hit {% endcase %}")
                .render("{ \"condition\":\"bad string here\" }"),
                is(""));
    }

    /*
     * def test_case_with_else
     *   assigns = {'condition' => 5 }
     *   assert_template_result(' hit ',
     *                          '{% case condition %}{% when 5 %} hit {% else %} else {% endcase %}',
     *                          assigns)
     *
     *   assigns = {'condition' => 6 }
     *   assert_template_result(' else ',
     *                          '{% case condition %}{% when 5 %} hit {% else %} else {% endcase %}',
     *                          assigns)
     *
     *   assigns = {'condition' => 6 }
     *   assert_template_result(' else ',
     *                          '{% case condition %} {% when 5 %} hit {% else %} else {% endcase %}',
     *                          assigns)
     * end
     */
    @Test
    public void case_with_elseTest() throws RecognitionException {

        assertThat(
                Template.parse("{% case condition %}{% when 5 %} hit {% else %} else {% endcase %}")
                .render("{ \"condition\":5 }"),
                is(" hit "));

        assertThat(
                Template.parse("{% case condition %}{% when 5 %} hit {% else %} else {% endcase %}")
                .render("{ \"condition\":6 }"),
                is(" else "));

        assertThat(
                Template.parse("{% case condition %} {% when 5 %} hit {% else %} else {% endcase %}")
                .render("{ \"condition\":6 }"),
                is(" else "));
    }

    /*
     * def test_case_on_size
     *   assert_template_result('',  '{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}', 'a' => [])
     *   assert_template_result('1', '{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}', 'a' => [1])
     *   assert_template_result('2', '{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}', 'a' => [1, 1])
     *   assert_template_result('',  '{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}', 'a' => [1, 1, 1])
     *   assert_template_result('',  '{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}', 'a' => [1, 1, 1, 1])
     *   assert_template_result('',  '{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}', 'a' => [1, 1, 1, 1, 1])
     * end
     */
    @Test
    public void case_on_sizeTest() throws RecognitionException {

        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}").render("{ \"a\":[] }"), is(""));
        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}").render("{ \"a\":[1] }"), is("1"));
        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}").render("{ \"a\":[1,1] }"), is("2"));
        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}").render("{ \"a\":[1,1,1] }"), is(""));
        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}").render("{ \"a\":[1,1,1,1] }"), is(""));
        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% endcase %}").render("{ \"a\":[1,1,1,1,1] }"), is(""));
    }

    /*
     * def test_case_on_size_with_else
     *   assert_template_result('else',
     *                          '{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}',
     *                          'a' => [])
     *
     *   assert_template_result('1',
     *                          '{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}',
     *                          'a' => [1])
     *
     *   assert_template_result('2',
     *                          '{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}',
     *                          'a' => [1, 1])
     *
     *   assert_template_result('else',
     *                          '{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}',
     *                          'a' => [1, 1, 1])
     *
     *   assert_template_result('else',
     *                          '{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}',
     *                          'a' => [1, 1, 1, 1])
     *
     *   assert_template_result('else',
     *                          '{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}',
     *                          'a' => [1, 1, 1, 1, 1])
     * end
     */
    @Test
    public void case_on_size_with_elseTest() throws RecognitionException {

        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}").render("{ \"a\":[] }"), is("else"));
        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}").render("{ \"a\":[1] }"), is("1"));
        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}").render("{ \"a\":[1,1] }"), is("2"));
        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}").render("{ \"a\":[1,1,1] }"), is("else"));
        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}").render("{ \"a\":[1,1,1,1] }"), is("else"));
        assertThat(Template.parse("{% case a.size %}{% when 1 %}1{% when 2 %}2{% else %}else{% endcase %}").render("{ \"a\":[1,1,1,1,1] }"), is("else"));
    }

    /*
     * def test_case_on_length_with_else
     *   assert_template_result('else',
     *                          '{% case a.empty? %}{% when true %}true{% when false %}false{% else %}else{% endcase %}',
     *                          {})
     *
     *   assert_template_result('false',
     *                          '{% case false %}{% when true %}true{% when false %}false{% else %}else{% endcase %}',
     *                          {})
     *
     *   assert_template_result('true',
     *                          '{% case true %}{% when true %}true{% when false %}false{% else %}else{% endcase %}',
     *                          {})
     *
     *   assert_template_result('else',
     *                          '{% case NULL %}{% when true %}true{% when false %}false{% else %}else{% endcase %}',
     *                          {})
     * end
     */
    @Test
    public void case_on_length_with_elseTest() throws RecognitionException {

        assertThat(Template.parse("{% case a.empty? %}{% when true %}true{% when false %}false{% else %}else{% endcase %}").render(), is("else"));
        assertThat(Template.parse("{% case false %}{% when true %}true{% when false %}false{% else %}else{% endcase %}").render(), is("false"));
        assertThat(Template.parse("{% case true %}{% when true %}true{% when false %}false{% else %}else{% endcase %}").render(), is("true"));
        assertThat(Template.parse("{% case NULL %}{% when true %}true{% when false %}false{% else %}else{% endcase %}").render(), is("else"));
    }

    /*
     * def test_assign_from_case
     *   # Example from the shopify forums
     *   code = %q({% case collection.handle %}{% when 'menswear-jackets' %}{% assign ptitle = 'menswear' %}{% when 'menswear-t-shirts' %}{% assign ptitle = 'menswear' %}{% else %}{% assign ptitle = 'womenswear' %}{% endcase %}{{ ptitle }})
     *   template = Liquid::Template.parse(code)
     *   assert_equal "menswear",   template.render("collection" => {'handle' => 'menswear-jackets'})
     *   assert_equal "menswear",   template.render("collection" => {'handle' => 'menswear-t-shirts'})
     *   assert_equal "womenswear", template.render("collection" => {'handle' => 'x'})
     *   assert_equal "womenswear", template.render("collection" => {'handle' => 'y'})
     *   assert_equal "womenswear", template.render("collection" => {'handle' => 'z'})
     * end
     */
    @Test
    public void assign_from_caseTest() throws RecognitionException {

        String code = "{% case collection.handle %}{% when 'menswear-jackets' %}{% assign ptitle = 'menswear' %}{% when 'menswear-t-shirts' %}{% assign ptitle = 'menswear' %}{% else %}{% assign ptitle = 'womenswear' %}{% endcase %}{{ ptitle }}";
        Template template = Template.parse(code);

        assertThat(template.render("{ \"collection\" : {\"handle\" : \"menswear-jackets\"} }"), is("menswear"));
        assertThat(template.render("{ \"collection\" : {\"handle\" : \"menswear-t-shirts\"} }"), is("menswear"));
        assertThat(template.render("{ \"handle\" : \"x\" }"), is("womenswear"));
        assertThat(template.render("{ \"handle\" : \"y\" }"), is("womenswear"));
        assertThat(template.render("{ \"handle\" : \"z\" }"), is("womenswear"));
    }

    /*
     * def test_case_when_or
     *   code = '{% case condition %}{% when 1 or 2 or 3 %} its 1 or 2 or 3 {% when 4 %} its 4 {% endcase %}'
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => 1 })
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => 2 })
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => 3 })
     *   assert_template_result(' its 4 ', code, {'condition' => 4 })
     *   assert_template_result('', code, {'condition' => 5 })
     *
     *   code = '{% case condition %}{% when 1 or "string" or null %} its 1 or 2 or 3 {% when 4 %} its 4 {% endcase %}'
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => 1 })
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => 'string' })
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => nil })
     *   assert_template_result('', code, {'condition' => 'something else' })
     * end
     */
    @Test
    public void case_when_orTest() throws RecognitionException {

        String code = "{% case condition %}{% when 1 or 2 or 3 %} its 1 or 2 or 3 {% when 4 %} its 4 {% endcase %}";

        assertThat(Template.parse(code).render("{ \"condition\" : 1 }"), is(" its 1 or 2 or 3 "));
        assertThat(Template.parse(code).render("{ \"condition\" : 2 }"), is(" its 1 or 2 or 3 "));
        assertThat(Template.parse(code).render("{ \"condition\" : 3 }"), is(" its 1 or 2 or 3 "));
        assertThat(Template.parse(code).render("{ \"condition\" : 4 }"), is(" its 4 "));
        assertThat(Template.parse(code).render("{ \"condition\" : 5 }"), is(""));
    }

    /*
     * def test_case_when_comma
     *   code = '{% case condition %}{% when 1, 2, 3 %} its 1 or 2 or 3 {% when 4 %} its 4 {% endcase %}'
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => 1 })
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => 2 })
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => 3 })
     *   assert_template_result(' its 4 ', code, {'condition' => 4 })
     *   assert_template_result('', code, {'condition' => 5 })
     *
     *   code = '{% case condition %}{% when 1, "string", null %} its 1 or 2 or 3 {% when 4 %} its 4 {% endcase %}'
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => 1 })
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => 'string' })
     *   assert_template_result(' its 1 or 2 or 3 ', code, {'condition' => nil })
     *   assert_template_result('', code, {'condition' => 'something else' })
     * end
     */
    @Test
    public void case_when_commaTest() throws RecognitionException {

        String code = "{% case condition %}{% when 1, 2, 3 %} its 1 or 2 or 3 {% when 4 %} its 4 {% endcase %}";

        assertThat(Template.parse(code).render("{ \"condition\" : 1 }"), is(" its 1 or 2 or 3 "));
        assertThat(Template.parse(code).render("{ \"condition\" : 2 }"), is(" its 1 or 2 or 3 "));
        assertThat(Template.parse(code).render("{ \"condition\" : 3 }"), is(" its 1 or 2 or 3 "));
        assertThat(Template.parse(code).render("{ \"condition\" : 4 }"), is(" its 4 "));
        assertThat(Template.parse(code).render("{ \"condition\" : 5 }"), is(""));

        code = "{% case condition %}{% when 1, \"string\", null %} its 1 or 2 or 3 {% when 4 %} its 4 {% endcase %}";

        assertThat(Template.parse(code).render("{ \"condition\" : 1 }"), is(" its 1 or 2 or 3 "));
        assertThat(Template.parse(code).render("{ \"condition\" : \"string\" }"), is(" its 1 or 2 or 3 "));
        assertThat(Template.parse(code).render("{ \"condition\" : null }"), is(" its 1 or 2 or 3 "));
        assertThat(Template.parse(code).render("{ \"condition\" : \"something else\" }"), is(""));
    }

    /*
     * def test_case_detects_bad_syntax
     *   assert_raise(SyntaxError) do
     *     assert_template_result('',  '{% case false %}{% when %}true{% endcase %}', {})
     *   end
     *
     *   assert_raise(SyntaxError) do
     *     assert_template_result('',  '{% case false %}{% huh %}true{% endcase %}', {})
     *   end
     *
     * end
     */
    @Test(expected = RuntimeException.class)
    public void case_detects_bad_syntax1Test() throws Exception {
        Template.parse("{% case false %}{% when %}true{% endcase %}");
    }

    @Test(expected = RuntimeException.class)
    public void case_detects_bad_syntax2Test() throws Exception {
        Template.parse("{% case false %}{% huh %}true{% endcase %}");
    }
}
