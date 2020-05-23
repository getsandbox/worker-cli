package org.jliquid.liqp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StatementsTest {

    /*
     * def test_true_eql_true
     *   text = %| {% if true == true %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render
     * end
     */
    @Test
    public void true_eql_trueTest() throws Exception {

        assertThat(Template.parse(" {% if true == true %} true {% else %} false {% endif %} ").render(), is("  true  "));
    }

    /*
     * def test_true_not_eql_true
     *   text = %| {% if true != true %} true {% else %} false {% endif %} |
     *   expected = %|  false  |
     *   assert_equal expected, Template.parse(text).render
     * end
     */
    @Test
    public void true_not_eql_trueTest() throws Exception {

        assertThat(Template.parse(" {% if true != true %} true {% else %} false {% endif %} ").render(), is("  false  "));
    }

    /*
     * def test_true_lq_true
     *   text = %| {% if 0 > 0 %} true {% else %} false {% endif %} |
     *   expected = %|  false  |
     *   assert_equal expected, Template.parse(text).render
     * end
     */
    @Test
    public void true_lq_trueTest() throws Exception {

        assertThat(Template.parse(" {% if 0 > 0 %} true {% else %} false {% endif %} ").render(), is("  false  "));
    }

    /*
     * def test_one_lq_zero
     *   text = %| {% if 1 > 0 %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render
     * end
     */
    @Test
    public void one_lq_zeroTest() throws Exception {

        assertThat(Template.parse(" {% if 1 > 0 %} true {% else %} false {% endif %} ").render(), is("  true  "));
    }

    /*
     * def test_zero_lq_one
     *   text = %| {% if 0 < 1 %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render
     * end
     */
    @Test
    public void zero_lq_oneTest() throws Exception {

        assertThat(Template.parse(" {% if 0 < 1 %} true {% else %} false {% endif %} ").render(), is("  true  "));
    }

    /*
     * def test_zero_lq_or_equal_one
     *   text = %| {% if 0 <= 0 %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render
     * end
     */
    @Test
    public void zero_lq_or_equal_oneTest() throws Exception {

        assertThat(Template.parse(" {% if 0 <= 0 %} true {% else %} false {% endif %} ").render(), is("  true  "));
    }

    /*
     * def test_zero_lq_or_equal_one_involving_nil
     *   text = %| {% if null <= 0 %} true {% else %} false {% endif %} |
     *   expected = %|  false  |
     *   assert_equal expected, Template.parse(text).render
     *
     *
     *   text = %| {% if 0 <= null %} true {% else %} false {% endif %} |
     *   expected = %|  false  |
     *   assert_equal expected, Template.parse(text).render
     * end
     */
    @Test
    public void zero_lq_or_equal_one_involving_nilTest() throws Exception {

        assertThat(Template.parse(" {% if null <= 0 %} true {% else %} false {% endif %} ").render(), is("  false  "));

        assertThat(Template.parse(" {% if 0 <= null %} true {% else %} false {% endif %} ").render(), is("  false  "));
    }

    /*
     * def test_zero_lqq_or_equal_one
     *   text = %| {% if 0 >= 0 %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render
     * end
     */
    @Test
    public void zero_lqq_or_equal_oneTest() throws Exception {

        assertThat(Template.parse(" {% if 0 >= 0 %} true {% else %} false {% endif %} ").render(), is("  true  "));
    }

    /*
     * def test_strings
     *   text = %| {% if 'test' == 'test' %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render
     * end
     */
    @Test
    public void stringsTest() throws Exception {

        assertThat(Template.parse(" {% if 'test' == 'test' %} true {% else %} false {% endif %} ").render(), is("  true  "));
    }

    /*
     * def test_strings_not_equal
     *   text = %| {% if 'test' != 'test' %} true {% else %} false {% endif %} |
     *   expected = %|  false  |
     *   assert_equal expected, Template.parse(text).render
     * end
     */
    @Test
    public void strings_not_equalTest() throws Exception {

        assertThat(Template.parse(" {% if 'test' != 'test' %} true {% else %} false {% endif %} ").render(), is("  false  "));
    }

    /*
     * def test_var_strings_equal
     *   text = %| {% if var == "hello there!" %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render('var' => 'hello there!')
     * end
     */
    @Test
    public void var_strings_equalTest() throws Exception {

        assertThat(
                Template.parse(" {% if var == \"hello there!\" %} true {% else %} false {% endif %} ")
                .render("{ \"var\" : \"hello there!\" }"),
                is("  true  "));
    }

    /*
     * def test_var_strings_are_not_equal
     *   text = %| {% if "hello there!" == var %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render('var' => 'hello there!')
     * end
     */
    @Test
    public void var_strings_are_not_equalTest() throws Exception {

        assertThat(
                Template.parse(" {% if \"hello there!\" == var %} true {% else %} false {% endif %} ")
                .render("{ \"var\" : \"hello there!\" }"),
                is("  true  "));
    }

    /*
     * def test_var_and_long_string_are_equal
     *   text = %| {% if var == 'hello there!' %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render('var' => 'hello there!')
     * end
     */
    @Test
    public void var_and_long_string_are_equalTest() throws Exception {

        assertThat(
                Template.parse(" {% if var == 'hello there!' %} true {% else %} false {% endif %} ")
                .render("{ \"var\" : \"hello there!\" }"),
                is("  true  "));
    }

    /*
     * def test_var_and_long_string_are_equal_backwards
     *   text = %| {% if 'hello there!' == var %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render('var' => 'hello there!')
     * end
     */
    @Test
    public void var_and_long_string_are_equal_backwardsTest() throws Exception {

        assertThat(
                Template.parse(" {% if 'hello there!' == var %} true {% else %} false {% endif %} ")
                .render("{ \"var\" : \"hello there!\" }"),
                is("  true  "));
    }

    /*
     * def test_is_collection_empty
     *   text = %| {% if array == empty %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render('array' => [])
     * end
     */
    @Test
    public void is_collection_emptyTest() throws Exception {

        assertThat(
                Template.parse(" {% if array == empty %} true {% else %} false {% endif %} ")
                .render("{ \"array\" : [] }"),
                is("  true  "));
    }

    /*
     * def test_is_not_collection_empty
     *   text = %| {% if array == empty %} true {% else %} false {% endif %} |
     *   expected = %|  false  |
     *   assert_equal expected, Template.parse(text).render('array' => [1,2,3])
     * end
     */
    @Test
    public void is_not_collection_emptyTest() throws Exception {

        assertThat(
                Template.parse(" {% if array == empty %} true {% else %} false {% endif %} ")
                .render("{ \"array\" : [1,2,3] }"),
                is("  false  "));
    }

    /*
     * def test_nil
     *   text = %| {% if var == nil %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render('var' => nil)
     *
     *   text = %| {% if var == null %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render('var' => nil)
     * end
     */
    @Test
    public void nilTest() throws Exception {

        assertThat(Template.parse(" {% if var == nil %} true {% else %} false {% endif %} ").render("{ \"var\" : null }"), is("  true  "));

        assertThat(Template.parse(" {% if var == null %} true {% else %} false {% endif %} ").render("{ \"var\" : null }"), is("  true  "));
    }

    /*
     * def test_not_nil
     *   text = %| {% if var != nil %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render('var' => 1 )
     *
     *   text = %| {% if var != null %} true {% else %} false {% endif %} |
     *   expected = %|  true  |
     *   assert_equal expected, Template.parse(text).render('var' => 1 )
     * end
     */
    @Test
    public void not_nilTest() throws Exception {

        assertThat(Template.parse(" {% if var != nil %} true {% else %} false {% endif %} ").render("{\"var\":1}"), is("  true  "));

        assertThat(Template.parse(" {% if var != null %} true {% else %} false {% endif %} ").render("{\"var\":1}"), is("  true  "));
    }
}
