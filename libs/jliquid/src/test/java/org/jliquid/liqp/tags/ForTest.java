package org.jliquid.liqp.tags;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ForTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{\"array\" : [1,2,3,4,5,6,7,8,9,10], \"item\" : {\"quantity\" : 5} }";

        String[][] tests = {
            {"{% for item in array %}{{ item }}{% endfor %}", "12345678910"},
            {"{% for item in array limit:8.5 %}{{ item }}{% endfor %}", "12345678"},
            {"{% for item in array limit:8.5 offset:6 %}{{ item }}{% endfor %}", "78910"},
            {"{% for item in array limit:2 offset:6 %}{{ item }}{% endfor %}", "78"},
            {"{% for i in (1..item.quantity) %}{{ i }}{% endfor %}", "12345"},
            {"{% for i in (1..3) %}{{ i }}{% endfor %}", "123"},
            {"{% for i in (1..nil) %}{{ i }}{% endfor %}", ""},
            {"{% for i in (XYZ .. 7) %}{{ i }}{% endfor %}", ""},
            {"{% for i in (1 .. item.quantity) offset:2 %}{{ i }}{% endfor %}", "345"},
            {"{% for i in (1.. item.quantity) offset:nil %}{{ i }}{% endfor %}", "12345"},
            {"{% for i in (1 ..item.quantity) limit:4 OFFSET:2 %}{{ i }}{% endfor %}", "1234"},
            {"{% for i in (1..item.quantity) limit:4 offset:20 %}{{ i }}{% endfor %}", ""},
            {"{% for i in (1..item.quantity) limit:0 offset:2 %}{{ i }}{% endfor %}", ""},
            {"{% for i in (1..5) limit:4 OFFSET:2 %}{{forloop.length}}{% endfor %}", "4444"},
            {"{% for i in array limit:4 OFFSET:2 %}{{forloop.length}}{% endfor %}", "4444"},
            {"{% for i in (1..5) limit:4 OFFSET:2 %}{{forloop.first}}{% endfor %}", "truefalsefalsefalse"},
            {"{% for i in array limit:4 OFFSET:2 %}{{forloop.first}}{% endfor %}", "truefalsefalsefalse"},
            {"{% for i in (1..5) limit:4 OFFSET:2 %}{{forloop.last}}{% endfor %}", "falsefalsefalsetrue"},
            {"{% for i in array limit:4 OFFSET:2 %}{{forloop.last}}{% endfor %}", "falsefalsefalsetrue"},
            {"{% for i in (1..5) limit:4 OFFSET:2 %}{{forloop.index}}{% endfor %}", "1234"},
            {"{% for i in array limit:4 OFFSET:2 %}{{forloop.index}}{% endfor %}", "1234"},
            {"{% for i in (1..5) limit:4 OFFSET:2 %}{{forloop.index0}}{% endfor %}", "0123"},
            {"{% for i in array limit:4 OFFSET:2 %}{{forloop.index0}}{% endfor %}", "0123"},
            {"{% for i in (1..5) limit:4 OFFSET:2 %}{{forloop.rindex}}{% endfor %}", "4321"},
            {"{% for i in array limit:4 OFFSET:2 %}{{forloop.rindex}}{% endfor %}", "4321"},
            {"{% for i in (1..5) limit:4 OFFSET:2 %}{{forloop.rindex0}}{% endfor %}", "3210"},
            {"{% for i in array limit:4 OFFSET:2 %}{{forloop.rindex0}}{% endfor %}", "3210"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_for
     *   assert_template_result(' yo  yo  yo  yo ','{%for item in array%} yo {%endfor%}','array' => [1,2,3,4])
     *   assert_template_result('yoyo','{%for item in array%}yo{%endfor%}','array' => [1,2])
     *   assert_template_result(' yo ','{%for item in array%} yo {%endfor%}','array' => [1])
     *   assert_template_result('','{%for item in array%}{%endfor%}','array' => [1,2])
     *   expected = <<HERE
     *
     *     yo
     *
     *     yo
     *
     *     yo
     *
     *   HERE
     *   template = <<HERE
     *   {%for item in array%}
     *     yo
     *   {%endfor%}
     *   HERE
     *   assert_template_result(expected,template,'array' => [1,2,3])
     * end
     */
    @Test
    public void forTest() throws RecognitionException {

        assertThat(Template.parse("{%for item in array%} yo {%endfor%}").render("{\"array\":[1,2,3,4]}"), is(" yo  yo  yo  yo "));
        assertThat(Template.parse("{%for item in array%}yo{%endfor%}").render("{\"array\":[1,2]}"), is("yoyo"));
        assertThat(Template.parse("{%for item in array%} yo {%endfor%}").render("{\"array\":[1]}"), is(" yo "));
        assertThat(Template.parse("{%for item in array%}{%endfor%}").render("{\"array\":[1,2]}"), is(""));
    }

    /*
     * def test_for_with_range
     *   assert_template_result(' 1  2  3 ','{%for item in (1..3) %} {{item}} {%endfor%}')
     * end
     */
    @Test
    public void forWithRangeTest() throws RecognitionException {

        assertThat(Template.parse("{%for item in (1..3) %} {{item}} {%endfor%}").render(), is(" 1  2  3 "));
    }

    /*
     * def test_for_with_variable
     *   assert_template_result(' 1  2  3 ','{%for item in array%} {{item}} {%endfor%}','array' => [1,2,3])
     *   assert_template_result('123','{%for item in array%}{{item}}{%endfor%}','array' => [1,2,3])
     *   assert_template_result('123','{% for item in array %}{{item}}{% endfor %}','array' => [1,2,3])
     *   assert_template_result('abcd','{%for item in array%}{{item}}{%endfor%}','array' => ['a','b','c','d'])
     *   assert_template_result('a b c','{%for item in array%}{{item}}{%endfor%}','array' => ['a',' ','b',' ','c'])
     *   assert_template_result('abc','{%for item in array%}{{item}}{%endfor%}','array' => ['a','','b','','c'])
     * end
     */
    @Test
    public void forWithVariableTest() throws RecognitionException {

        assertThat(Template.parse("{%for item in array%} {{item}} {%endfor%}").render("{\"array\":[1,2,3]}"), is(" 1  2  3 "));
        assertThat(Template.parse("{%for item in array%}{{item}}{%endfor%}").render("{\"array\":[1,2,3]}"), is("123"));
        assertThat(Template.parse("{% for item in array %}{{item}}{% endfor %}").render("{\"array\":[1,2,3]}"), is("123"));
        assertThat(Template.parse("{%for item in array%}{{item}}{%endfor%}").render("{\"array\":[\"a\",\"b\",\"c\",\"d\"]}"), is("abcd"));
        assertThat(Template.parse("{%for item in array%}{{item}}{%endfor%}").render("{\"array\":[\"a\",\" \",\"b\",\" \",\"c\"]}"), is("a b c"));
        assertThat(Template.parse("{%for item in array%}{{item}}{%endfor%}").render("{\"array\":[\"a\",\"\",\"b\",\"\",\"c\"]}"), is("abc"));
    }

    @Test
    public void forWithMapIndexAccessTest() throws RecognitionException {

        assertThat(Template.parse("{%for item in map%}{{item[0]}}-{{item[1]}}{%endfor%}").render("{\"map\":{ \"key\": \"value\"} }"), is("key-value"));

    }

    /*
     * def test_for_helpers
     *   assigns = {'array' => [1,2,3] }
     *   assert_template_result(' 1/3  2/3  3/3 ',
     *                          '{%for item in array%} {{forloop.index}}/{{forloop.length}} {%endfor%}',
     *                          assigns)
     *   assert_template_result(' 1  2  3 ', '{%for item in array%} {{forloop.index}} {%endfor%}', assigns)
     *   assert_template_result(' 0  1  2 ', '{%for item in array%} {{forloop.index0}} {%endfor%}', assigns)
     *   assert_template_result(' 2  1  0 ', '{%for item in array%} {{forloop.rindex0}} {%endfor%}', assigns)
     *   assert_template_result(' 3  2  1 ', '{%for item in array%} {{forloop.rindex}} {%endfor%}', assigns)
     *   assert_template_result(' true  false  false ', '{%for item in array%} {{forloop.first}} {%endfor%}', assigns)
     *   assert_template_result(' false  false  true ', '{%for item in array%} {{forloop.last}} {%endfor%}', assigns)
     * end
     */
    @Test
    public void forHelpersTest() throws RecognitionException {

        final String assigns = "{\"array\":[1,2,3]}";

        assertThat(Template.parse("{%for item in array%} {{forloop.index}}/{{forloop.length}} {%endfor%}").render(assigns), is(" 1/3  2/3  3/3 "));
        assertThat(Template.parse("{%for item in array%} {{forloop.index}} {%endfor%}").render(assigns), is(" 1  2  3 "));
        assertThat(Template.parse("{%for item in array%} {{forloop.index0}} {%endfor%}").render(assigns), is(" 0  1  2 "));
        assertThat(Template.parse("{%for item in array%} {{forloop.rindex0}} {%endfor%}").render(assigns), is(" 2  1  0 "));
        assertThat(Template.parse("{%for item in array%} {{forloop.rindex}} {%endfor%}").render(assigns), is(" 3  2  1 "));
        assertThat(Template.parse("{%for item in array%} {{forloop.first}} {%endfor%}").render(assigns), is(" true  false  false "));
        assertThat(Template.parse("{%for item in array%} {{forloop.last}} {%endfor%}").render(assigns), is(" false  false  true "));
    }

    /*
     * def test_for_and_if
     *   assigns = {'array' => [1,2,3] }
     *   assert_template_result('+--',
     *                          '{%for item in array%}{% if forloop.first %}+{% else %}-{% endif %}{%endfor%}',
     *                          assigns)
     * end
     */
    @Test
    public void forAndIfTest() throws RecognitionException {

        final String assigns = "{\"array\":[1,2,3]}";

        assertThat(Template.parse("{%for item in array%}{% if forloop.first %}+{% else %}-{% endif %}{%endfor%}").render(assigns), is("+--"));
    }

    /*
     * def test_for_else
     *   assert_template_result('+++', '{%for item in array%}+{%else%}-{%endfor%}', 'array'=>[1,2,3])
     *   assert_template_result('-',   '{%for item in array%}+{%else%}-{%endfor%}', 'array'=>[])
     *   assert_template_result('-',   '{%for item in array%}+{%else%}-{%endfor%}', 'array'=>nil)
     * end
     */
    @Test
    public void forElseTest() throws RecognitionException {

        assertThat(Template.parse("{%for item in array%}+{%else%}-{%endfor%}").render("{\"array\":[1,2,3]}"), is("+++"));
        assertThat(Template.parse("{%for item in array%}+{%else%}-{%endfor%}").render("{\"array\":[]}"), is("-"));
        assertThat(Template.parse("{%for item in array%}+{%else%}-{%endfor%}").render("{\"array\":null}"), is("-"));
    }

    /*
     * def test_limiting
     *   assigns = {'array' => [1,2,3,4,5,6,7,8,9,0]}
     *   assert_template_result('12', '{%for i in array limit:2 %}{{ i }}{%endfor%}', assigns)
     *   assert_template_result('1234', '{%for i in array limit:4 %}{{ i }}{%endfor%}', assigns)
     *   assert_template_result('3456', '{%for i in array limit:4 offset:2 %}{{ i }}{%endfor%}', assigns)
     *   assert_template_result('3456', '{%for i in array limit: 4 offset: 2 %}{{ i }}{%endfor%}', assigns)
     * end
     */
    @Test
    public void limitingTest() throws RecognitionException {

        final String assigns = "{\"array\":[1,2,3,4,5,6,7,8,9,0]}";

        assertThat(Template.parse("{%for i in array limit:2 %}{{ i }}{%endfor%}").render(assigns), is("12"));
        assertThat(Template.parse("{%for i in array limit:4 %}{{ i }}{%endfor%}").render(assigns), is("1234"));
        assertThat(Template.parse("{%for i in array limit:4 offset:2 %}{{ i }}{%endfor%}").render(assigns), is("3456"));
        assertThat(Template.parse("{%for i in array limit: 4 offset: 2 %}{{ i }}{%endfor%}").render(assigns), is("3456"));
    }

    /*
     * def test_dynamic_variable_limiting
     *   assigns = {'array' => [1,2,3,4,5,6,7,8,9,0]}
     *   assigns['limit'] = 2
     *   assigns['offset'] = 2
     *
     *   assert_template_result('34', '{%for i in array limit: limit offset: offset %}{{ i }}{%endfor%}', assigns)
     * end
     */
    @Test
    public void dynamicVariableLimitingTest() throws RecognitionException {

        final String assigns = "{ \"array\":[1,2,3,4,5,6,7,8,9,0], \"limit\":2, \"offset\":2 }";

        assertThat(Template.parse("{%for i in array limit: limit offset: offset %}{{ i }}{%endfor%}").render(assigns), is("34"));
    }

    /*
     * def test_nested_for
     *   assigns = {'array' => [[1,2],[3,4],[5,6]] }
     *   assert_template_result('123456', '{%for item in array%}{%for i in item%}{{ i }}{%endfor%}{%endfor%}', assigns)
     * end
     */
    @Test
    public void nestedForTest() throws RecognitionException {

        final String assigns = "{ \"array\":[[1,2], [3,4], [5,6]] }";

        assertThat(Template.parse("{%for item in array%}{%for i in item%}{{ i }}{%endfor%}{%endfor%}").render(assigns), is("123456"));
    }

    /*
     * def test_offset_only
     *   assigns = {'array' => [1,2,3,4,5,6,7,8,9,0]}
     *   assert_template_result('890', '{%for i in array offset:7 %}{{ i }}{%endfor%}', assigns)
     * end
     */
    @Test
    public void offsetOnlyTest() throws RecognitionException {

        final String assigns = "{ \"array\":[1,2,3,4,5,6,7,8,9,0] }";

        assertThat(Template.parse("{%for i in array offset:7 %}{{ i }}{%endfor%}").render(assigns), is("890"));
    }

    /*
     * def test_pause_resume
     *   assigns = {'array' => {'items' => [1,2,3,4,5,6,7,8,9,0]}}
     *   markup = <<-MKUP
     *     {%for i in array.items limit: 3 %}{{i}}{%endfor%}
     *     next
     *     {%for i in array.items offset:continue limit: 3 %}{{i}}{%endfor%}
     *     next
     *     {%for i in array.items offset:continue limit: 3 %}{{i}}{%endfor%}
     *     MKUP
     *   expected = <<-XPCTD
     *     123
     *     next
     *     456
     *     next
     *     789
     *     XPCTD
     *   assert_template_result(expected,markup,assigns)
     * end
     */
    @Test
    public void pauseResumeTest() throws RecognitionException {

        final String assigns = "{ \"array\": { \"items\":[1,2,3,4,5,6,7,8,9,0] } }";

        final String markup = "{%for i in array.items limit: 3 %}{{i}}{%endfor%}\n"
                + "next\n"
                + "{%for i in array.items offset:continue limit: 3 %}{{i}}{%endfor%}\n"
                + "next\n"
                + "{%for i in array.items offset:continue limit: 3 %}{{i}}{%endfor%}";

        final String expected = "123\n"
                + "next\n"
                + "456\n"
                + "next\n"
                + "789";

        assertThat(Template.parse(markup).render(assigns), is(expected));
    }

    /*
     * def test_pause_resume_limit
     *   assigns = {'array' => {'items' => [1,2,3,4,5,6,7,8,9,0]}}
     *   markup = <<-MKUP
     *     {%for i in array.items limit:3 %}{{i}}{%endfor%}
     *     next
     *     {%for i in array.items offset:continue limit:3 %}{{i}}{%endfor%}
     *     next
     *     {%for i in array.items offset:continue limit:1 %}{{i}}{%endfor%}
     *     MKUP
     *   expected = <<-XPCTD
     *     123
     *     next
     *     456
     *     next
     *     7
     *     XPCTD
     *   assert_template_result(expected,markup,assigns)
     * end
     */
    @Test
    public void pauseResumeLimitTest() throws RecognitionException {

        final String assigns = "{ \"array\": { \"items\":[1,2,3,4,5,6,7,8,9,0] } }";

        final String markup = "{%for i in array.items limit:3 %}{{i}}{%endfor%}\n"
                + "next\n"
                + "{%for i in array.items offset:continue limit:3 %}{{i}}{%endfor%}\n"
                + "next\n"
                + "{%for i in array.items offset:continue limit:1 %}{{i}}{%endfor%}";

        final String expected = "123\n"
                + "next\n"
                + "456\n"
                + "next\n"
                + "7";

        assertThat(Template.parse(markup).render(assigns), is(expected));
    }

    /*
     * def test_pause_resume_BIG_limit
     *   assigns = {'array' => {'items' => [1,2,3,4,5,6,7,8,9,0]}}
     *   markup = <<-MKUP
     *     {%for i in array.items limit:3 %}{{i}}{%endfor%}
     *     next
     *     {%for i in array.items offset:continue limit:3 %}{{i}}{%endfor%}
     *     next
     *     {%for i in array.items offset:continue limit:1000 %}{{i}}{%endfor%}
     *     MKUP
     *   expected = <<-XPCTD
     *     123
     *     next
     *     456
     *     next
     *     7890
     *     XPCTD
     *     assert_template_result(expected,markup,assigns)
     * end
     */
    @Test
    public void pauseResumeBigLimitTest() throws RecognitionException {

        final String assigns = "{ \"array\": { \"items\":[1,2,3,4,5,6,7,8,9,0] } }";

        final String markup = "{%for i in array.items limit:3 %}{{i}}{%endfor%}\n"
                + "next\n"
                + "{%for i in array.items offset:continue limit:3 %}{{i}}{%endfor%}\n"
                + "next\n"
                + "{%for i in array.items offset:continue limit:1000 %}{{i}}{%endfor%}";

        final String expected = "123\n"
                + "next\n"
                + "456\n"
                + "next\n"
                + "7890";

        assertThat(Template.parse(markup).render(assigns), is(expected));
    }

    /*
     * def test_pause_resume_BIG_offset
     *   assigns = {'array' => {'items' => [1,2,3,4,5,6,7,8,9,0]}}
     *   markup = %q({%for i in array.items limit:3 %}{{i}}{%endfor%}
     *     next
     *     {%for i in array.items offset:continue limit:3 %}{{i}}{%endfor%}
     *     next
     *     {%for i in array.items offset:continue limit:3 offset:1000 %}{{i}}{%endfor%})
     *   expected = %q(123
     *     next
     *     456
     *     next
     *     )
     *     assert_template_result(expected,markup,assigns)
     * end
     */
    @Test
    public void pauseResumeBigOffsetTest() throws RecognitionException {

        final String assigns = "{ \"array\": { \"items\":[1,2,3,4,5,6,7,8,9,0] } }";

        final String markup = "{%for i in array.items limit:3 %}{{i}}{%endfor%}\n"
                + "next\n"
                + "{%for i in array.items offset:continue limit:3 %}{{i}}{%endfor%}\n"
                + "next\n"
                + "{%for i in array.items offset:continue limit:3 offset:1000 %}{{i}}{%endfor%}";

        final String expected = "123\n"
                + "next\n"
                + "456\n"
                + "next\n";

        assertThat(Template.parse(markup).render(assigns), is(expected));
    }

    /*
     * def test_for_with_break
     *   assigns = {'array' => {'items' => [1,2,3,4,5,6,7,8,9,10]}}
     *
     *   markup = '{% for i in array.items %}{% break %}{% endfor %}'
     *   expected = ""
     *   assert_template_result(expected,markup,assigns)
     *
     *   markup = '{% for i in array.items %}{{ i }}{% break %}{% endfor %}'
     *   expected = "1"
     *   assert_template_result(expected,markup,assigns)
     *
     *   markup = '{% for i in array.items %}{% break %}{{ i }}{% endfor %}'
     *   expected = ""
     *   assert_template_result(expected,markup,assigns)
     *
     *   markup = '{% for i in array.items %}{{ i }}{% if i > 3 %}{% break %}{% endif %}{% endfor %}'
     *   expected = "1234"
     *   assert_template_result(expected,markup,assigns)
     *
     *   # tests to ensure it only breaks out of the local for loop
     *   # and not all of them.
     *   assigns = {'array' => [[1,2],[3,4],[5,6]] }
     *   markup = '{% for item in array %}' +
     *              '{% for i in item %}' +
     *                '{% if i == 1 %}' +
     *                  '{% break %}' +
     *                '{% endif %}' +
     *                '{{ i }}' +
     *              '{% endfor %}' +
     *            '{% endfor %}'
     *   expected = '3456'
     *   assert_template_result(expected, markup, assigns)
     *
     *   # test break does nothing when unreached
     *   assigns = {'array' => {'items' => [1,2,3,4,5]}}
     *   markup = '{% for i in array.items %}{% if i == 9999 %}{% break %}{% endif %}{{ i }}{% endfor %}'
     *   expected = '12345'
     *   assert_template_result(expected, markup, assigns)
     * end
     */
    @Test
    public void forWithBreakTest() throws RecognitionException {

        String assigns = "{ \"array\": { \"items\":[1,2,3,4,5,6,7,8,9,0] } }";

        String markup = "{% for i in array.items %}{% break %}{% endfor %}";
        String expected = "";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        markup = "{% for i in array.items %}{{ i }}{% break %}{% endfor %}";
        expected = "1";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        markup = "{% for i in array.items %}{% break %}{{ i }}{% endfor %}";
        expected = "";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        markup = "{% for i in array.items %}{{ i }}{% if i > 3 %}{% break %}{% endif %}{% endfor %}";
        expected = "1234";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        assigns = "{ \"array\":[[1,2],[3,4],[5,6]] }";

        markup = "{% for item in array %}"
                + "{% for i in item %}"
                + "{% if i == 1 %}"
                + "{% break %}"
                + "{% endif %}"
                + "{{ i }}"
                + "{% endfor %}"
                + "{% endfor %}";
        expected = "3456";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        assigns = "{ \"array\": { \"items\":[1,2,3,4,5] } }";

        markup = "{% for i in array.items %}{% if i == 9999 %}{% break %}{% endif %}{{ i }}{% endfor %}";
        expected = "12345";
        assertThat(Template.parse(markup).render(assigns), is(expected));
    }

    /*
     * def test_for_with_continue
     *   assigns = {'array' => {'items' => [1,2,3,4,5]}}
     *
     *   markup = '{% for i in array.items %}{% continue %}{% endfor %}'
     *   expected = ""
     *   assert_template_result(expected,markup,assigns)
     *
     *   markup = '{% for i in array.items %}{{ i }}{% continue %}{% endfor %}'
     *   expected = "12345"
     *   assert_template_result(expected,markup,assigns)
     *
     *   markup = '{% for i in array.items %}{% continue %}{{ i }}{% endfor %}'
     *   expected = ""
     *   assert_template_result(expected,markup,assigns)
     *
     *   markup = '{% for i in array.items %}{% if i > 3 %}{% continue %}{% endif %}{{ i }}{% endfor %}'
     *   expected = "123"
     *   assert_template_result(expected,markup,assigns)
     *
     *   markup = '{% for i in array.items %}{% if i == 3 %}{% continue %}{% else %}{{ i }}{% endif %}{% endfor %}'
     *   expected = "1245"
     *   assert_template_result(expected,markup,assigns)
     *
     *   # tests to ensure it only continues the local for loop and not all of them.
     *   assigns = {'array' => [[1,2],[3,4],[5,6]] }
     *   markup = '{% for item in array %}' +
     *              '{% for i in item %}' +
     *                '{% if i == 1 %}' +
     *                  '{% continue %}' +
     *                '{% endif %}' +
     *                '{{ i }}' +
     *              '{% endfor %}' +
     *            '{% endfor %}'
     *   expected = '23456'
     *   assert_template_result(expected, markup, assigns)
     *
     *   # test continue does nothing when unreached
     *   assigns = {'array' => {'items' => [1,2,3,4,5]}}
     *   markup = '{% for i in array.items %}{% if i == 9999 %}{% continue %}{% endif %}{{ i }}{% endfor %}'
     *   expected = '12345'
     *   assert_template_result(expected, markup, assigns)
     * end
     */
    @Test
    public void forWithContinueTest() throws RecognitionException {

        String assigns = "{ \"array\": { \"items\":[1,2,3,4,5] } }";

        String markup = "{% for i in array.items %}{% continue %}{% endfor %}";
        String expected = "";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        markup = "{% for i in array.items %}{{ i }}{% continue %}{% endfor %}";
        expected = "12345";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        markup = "{% for i in array.items %}{% continue %}{{ i }}{% endfor %}";
        expected = "";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        markup = "{% for i in array.items %}{% if i > 3 %}{% continue %}{% endif %}{{ i }}{% endfor %}";
        expected = "123";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        markup = "{% for i in array.items %}{% if i == 3 %}{% continue %}{% else %}{{ i }}{% endif %}{% endfor %}";
        expected = "1245";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        assigns = "{ \"array\":[[1,2],[3,4],[5,6]] }";

        markup = "{% for item in array %}"
                + "{% for i in item %}"
                + "{% if i == 1 %}"
                + "{% continue %}"
                + "{% endif %}"
                + "{{ i }}"
                + "{% endfor %}"
                + "{% endfor %}";
        expected = "23456";
        assertThat(Template.parse(markup).render(assigns), is(expected));

        assigns = "{ \"array\": { \"items\":[1,2,3,4,5] } }";

        markup = "{% for i in array.items %}{% if i == 9999 %}{% continue %}{% endif %}{{ i }}{% endfor %}";
        expected = "12345";
        assertThat(Template.parse(markup).render(assigns), is(expected));
    }

    /*
     * def test_for_tag_string
     *   # ruby 1.8.7 "String".each => Enumerator with single "String" element.
     *   # ruby 1.9.3 no longer supports .each on String though we mimic
     *   # the functionality for backwards compatibility
     *
     *   assert_template_result('test string',
     *               '{%for val in string%}{{val}}{%endfor%}',
     *               'string' => "test string")
     *
     *   assert_template_result('test string',
     *               '{%for val in string limit:1%}{{val}}{%endfor%}',
     *               'string' => "test string")
     *
     *   assert_template_result('val-string-1-1-0-1-0-true-true-test string',
     *               '{%for val in string%}' +
     *               '{{forloop.name}}-' +
     *               '{{forloop.index}}-' +
     *               '{{forloop.length}}-' +
     *               '{{forloop.index0}}-' +
     *               '{{forloop.rindex}}-' +
     *               '{{forloop.rindex0}}-' +
     *               '{{forloop.first}}-' +
     *               '{{forloop.last}}-' +
     *               '{{val}}{%endfor%}',
     *               'string' => "test string")
     * end
     */
    @Test
    public void forTagStringTest() throws RecognitionException {

        String json = "{ \"string\":\"test string\" }";

        assertThat(Template.parse("{%for val in string%}{{val}}{%endfor%}").render(json), is("test string"));

        assertThat(Template.parse("{%for val in string limit:1%}{{val}}{%endfor%}").render(json), is("test string"));

        assertThat(Template.parse("{%for val in string%}"
                + "{{forloop.name}}-"
                + "{{forloop.index}}-"
                + "{{forloop.length}}-"
                + "{{forloop.index0}}-"
                + "{{forloop.rindex}}-"
                + "{{forloop.rindex0}}-"
                + "{{forloop.first}}-"
                + "{{forloop.last}}-"
                + "{{val}}{%endfor%}").render(json), is("val-string-1-1-0-1-0-true-true-test string"));

        // extra `name` testjson = "{ \"string\":\"test string\" }";
        json = "{ \"X\": [ { \"Y\":\"foo\"}, \"test string\" ] }";
        assertThat(Template.parse("{% for x in X[0].Y %}{{forloop.name}}-{{x}}{%endfor%}").render(json),
                is("x-X[0].Y-foo"));
    }

    /*
     * def test_blank_string_not_iterable
     *   assert_template_result('', "{% for char in characters %}I WILL NOT BE OUTPUT{% endfor %}", 'characters' => '')
     * end
     */
    @Test
    public void blankStringNotIterableTest() throws RecognitionException {

        assertThat(Template.parse("{% for char in characters %}I WILL NOT BE OUTPUT{% endfor %}").render(), is(""));
    }
}
