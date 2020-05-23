package org.jliquid.liqp.tags;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TablerowTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{\"products\" : [ {\"name\":\"a\"}, {\"name\":\"b\"}, " +
                "{\"name\":\"c\"}, {\"name\":\"d\"}, {\"name\":\"e\"}, " +
                "{\"name\":\"f\"}, {\"name\":\"g\"}, {\"name\":\"h\"} ] }";

        String[][] tests = {
                {"{% tablerow p in products %}\n" +
                        "{{ p.name }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "a\n" +
                                "</td><td class=\"col2\">\n" +
                                "b\n" +
                                "</td><td class=\"col3\">\n" +
                                "c\n" +
                                "</td><td class=\"col4\">\n" +
                                "d\n" +
                                "</td><td class=\"col5\">\n" +
                                "e\n" +
                                "</td><td class=\"col6\">\n" +
                                "f\n" +
                                "</td><td class=\"col7\">\n" +
                                "g\n" +
                                "</td><td class=\"col8\">\n" +
                                "h\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products %}\n" +
                        "{{ tablerowloop.length }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "8\n" +
                                "</td><td class=\"col2\">\n" +
                                "8\n" +
                                "</td><td class=\"col3\">\n" +
                                "8\n" +
                                "</td><td class=\"col4\">\n" +
                                "8\n" +
                                "</td><td class=\"col5\">\n" +
                                "8\n" +
                                "</td><td class=\"col6\">\n" +
                                "8\n" +
                                "</td><td class=\"col7\">\n" +
                                "8\n" +
                                "</td><td class=\"col8\">\n" +
                                "8\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products cols:3 %}\n" +
                        "{{ tablerowloop.length }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "8\n" +
                                "</td><td class=\"col2\">\n" +
                                "8\n" +
                                "</td><td class=\"col3\">\n" +
                                "8\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row2\"><td class=\"col1\">\n" +
                                "8\n" +
                                "</td><td class=\"col2\">\n" +
                                "8\n" +
                                "</td><td class=\"col3\">\n" +
                                "8\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row3\"><td class=\"col1\">\n" +
                                "8\n" +
                                "</td><td class=\"col2\">\n" +
                                "8\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products cols:3 %}\n" +
                        "{{ tablerowloop.index }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "1\n" +
                                "</td><td class=\"col2\">\n" +
                                "2\n" +
                                "</td><td class=\"col3\">\n" +
                                "3\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row2\"><td class=\"col1\">\n" +
                                "4\n" +
                                "</td><td class=\"col2\">\n" +
                                "5\n" +
                                "</td><td class=\"col3\">\n" +
                                "6\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row3\"><td class=\"col1\">\n" +
                                "7\n" +
                                "</td><td class=\"col2\">\n" +
                                "8\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products cols:3 %}\n" +
                        "{{ tablerowloop.index0 }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "0\n" +
                                "</td><td class=\"col2\">\n" +
                                "1\n" +
                                "</td><td class=\"col3\">\n" +
                                "2\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row2\"><td class=\"col1\">\n" +
                                "3\n" +
                                "</td><td class=\"col2\">\n" +
                                "4\n" +
                                "</td><td class=\"col3\">\n" +
                                "5\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row3\"><td class=\"col1\">\n" +
                                "6\n" +
                                "</td><td class=\"col2\">\n" +
                                "7\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products cols:3 limit:4 %}\n" +
                        "{{ tablerowloop.index0 }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "0\n" +
                                "</td><td class=\"col2\">\n" +
                                "1\n" +
                                "</td><td class=\"col3\">\n" +
                                "2\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row2\"><td class=\"col1\">\n" +
                                "3\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products cols:3 limit:400000000 %}\n" +
                        "{{ tablerowloop.index0 }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "0\n" +
                                "</td><td class=\"col2\">\n" +
                                "1\n" +
                                "</td><td class=\"col3\">\n" +
                                "2\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row2\"><td class=\"col1\">\n" +
                                "3\n" +
                                "</td><td class=\"col2\">\n" +
                                "4\n" +
                                "</td><td class=\"col3\">\n" +
                                "5\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row3\"><td class=\"col1\">\n" +
                                "6\n" +
                                "</td><td class=\"col2\">\n" +
                                "7\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products cols:3 limit:4 %}\n" +
                        "{{ tablerowloop.rindex0 }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "3\n" +
                                "</td><td class=\"col2\">\n" +
                                "2\n" +
                                "</td><td class=\"col3\">\n" +
                                "1\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row2\"><td class=\"col1\">\n" +
                                "0\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products %}\n" +
                        "{{ tablerowloop.rindex0 }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "7\n" +
                                "</td><td class=\"col2\">\n" +
                                "6\n" +
                                "</td><td class=\"col3\">\n" +
                                "5\n" +
                                "</td><td class=\"col4\">\n" +
                                "4\n" +
                                "</td><td class=\"col5\">\n" +
                                "3\n" +
                                "</td><td class=\"col6\">\n" +
                                "2\n" +
                                "</td><td class=\"col7\">\n" +
                                "1\n" +
                                "</td><td class=\"col8\">\n" +
                                "0\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products %}\n" +
                        "{{ tablerowloop.rindex }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "8\n" +
                                "</td><td class=\"col2\">\n" +
                                "7\n" +
                                "</td><td class=\"col3\">\n" +
                                "6\n" +
                                "</td><td class=\"col4\">\n" +
                                "5\n" +
                                "</td><td class=\"col5\">\n" +
                                "4\n" +
                                "</td><td class=\"col6\">\n" +
                                "3\n" +
                                "</td><td class=\"col7\">\n" +
                                "2\n" +
                                "</td><td class=\"col8\">\n" +
                                "1\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products %}\n" +
                        "{{ tablerowloop.first }}-{{ tablerowloop.last }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "true-false\n" +
                                "</td><td class=\"col2\">\n" +
                                "false-false\n" +
                                "</td><td class=\"col3\">\n" +
                                "false-false\n" +
                                "</td><td class=\"col4\">\n" +
                                "false-false\n" +
                                "</td><td class=\"col5\">\n" +
                                "false-false\n" +
                                "</td><td class=\"col6\">\n" +
                                "false-false\n" +
                                "</td><td class=\"col7\">\n" +
                                "false-false\n" +
                                "</td><td class=\"col8\">\n" +
                                "false-true\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products cols:3 %}\n" +
                        "{{ tablerowloop.col0 }}-{{ tablerowloop.col }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "0-1\n" +
                                "</td><td class=\"col2\">\n" +
                                "1-2\n" +
                                "</td><td class=\"col3\">\n" +
                                "2-3\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row2\"><td class=\"col1\">\n" +
                                "0-1\n" +
                                "</td><td class=\"col2\">\n" +
                                "1-2\n" +
                                "</td><td class=\"col3\">\n" +
                                "2-3\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row3\"><td class=\"col1\">\n" +
                                "0-1\n" +
                                "</td><td class=\"col2\">\n" +
                                "1-2\n" +
                                "</td></tr>\n"},

                {"{% tablerow p in products cols:3 %}\n" +
                        "{{ tablerowloop.col_first }}-{{ tablerowloop.col_last }}\n" +
                        "{% endtablerow %}",
                        "<tr class=\"row1\">\n" +
                                "<td class=\"col1\">\n" +
                                "true-false\n" +
                                "</td><td class=\"col2\">\n" +
                                "false-false\n" +
                                "</td><td class=\"col3\">\n" +
                                "false-true\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row2\"><td class=\"col1\">\n" +
                                "true-false\n" +
                                "</td><td class=\"col2\">\n" +
                                "false-false\n" +
                                "</td><td class=\"col3\">\n" +
                                "false-true\n" +
                                "</td></tr>\n" +
                                "<tr class=\"row3\"><td class=\"col1\">\n" +
                                "true-false\n" +
                                "</td><td class=\"col2\">\n" +
                                "false-false\n" +
                                "</td></tr>\n"},
        };

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));
            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_html_table
     *
     *   assert_template_result("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 4 </td><td class=\"col2\"> 5 </td><td class=\"col3\"> 6 </td></tr>\n",
     *                          '{% tablerow n in numbers cols:3%} {{n}} {% endtablerow %}',
     *                          'numbers' => [1,2,3,4,5,6])
     *
     *   assert_template_result("<tr class=\"row1\">\n</tr>\n",
     *                          '{% tablerow n in numbers cols:3%} {{n}} {% endtablerow %}',
     *                          'numbers' => [])
     * end
     */
    @Test
    public void htmlTableTest() throws RecognitionException {

        assertThat(
                Template.parse("{% tablerow n in numbers cols:3%} {{n}} {% endtablerow %}")
                        .render("{ \"numbers\":[1,2,3,4,5,6] }"),
                is("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 4 </td><td class=\"col2\"> 5 </td><td class=\"col3\"> 6 </td></tr>\n"));

        assertThat(
                Template.parse("{% tablerow n in numbers cols:3%} {{n}} {% endtablerow %}")
                        .render("{ \"numbers\":[] }"),
                is("<tr class=\"row1\">\n</tr>\n"));
    }

    /*
     * def test_html_table_with_different_cols
     *   assert_template_result("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td><td class=\"col4\"> 4 </td><td class=\"col5\"> 5 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 6 </td></tr>\n",
     *                          '{% tablerow n in numbers cols:5%} {{n}} {% endtablerow %}',
     *                          'numbers' => [1,2,3,4,5,6])
     *
     * end
     */
    @Test
    public void htmlTableWithDifferentColsTest() throws RecognitionException {

        assertThat(
                Template.parse("{% tablerow n in numbers cols:5%} {{n}} {% endtablerow %}")
                        .render("{ \"numbers\":[1,2,3,4,5,6] }"),
                is("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td><td class=\"col4\"> 4 </td><td class=\"col5\"> 5 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 6 </td></tr>\n"));
    }

    /*
     * def test_html_col_counter
     *   assert_template_result("<tr class=\"row1\">\n<td class=\"col1\">1</td><td class=\"col2\">2</td></tr>\n<tr class=\"row2\"><td class=\"col1\">1</td><td class=\"col2\">2</td></tr>\n<tr class=\"row3\"><td class=\"col1\">1</td><td class=\"col2\">2</td></tr>\n",
     *                          '{% tablerow n in numbers cols:2%}{{tablerowloop.col}}{% endtablerow %}',
     *                          'numbers' => [1,2,3,4,5,6])
     * end
     */
    @Test
    public void htmlColCounterTest() throws RecognitionException {

        assertThat(
                Template.parse("{% tablerow n in numbers cols:2%}{{tablerowloop.col}}{% endtablerow %}")
                        .render("{ \"numbers\":[1,2,3,4,5,6] }"),
                is("<tr class=\"row1\">\n<td class=\"col1\">1</td><td class=\"col2\">2</td></tr>\n<tr class=\"row2\"><td class=\"col1\">1</td><td class=\"col2\">2</td></tr>\n<tr class=\"row3\"><td class=\"col1\">1</td><td class=\"col2\">2</td></tr>\n"));
    }

    /*
     * def test_quoted_fragment
     *   assert_template_result("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 4 </td><td class=\"col2\"> 5 </td><td class=\"col3\"> 6 </td></tr>\n",
     *                          "{% tablerow n in collections.frontpage cols:3%} {{n}} {% endtablerow %}",
     *                          'collections' => {'frontpage' => [1,2,3,4,5,6]})
     *   assert_template_result("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 4 </td><td class=\"col2\"> 5 </td><td class=\"col3\"> 6 </td></tr>\n",
     *                          "{% tablerow n in collections['frontpage'] cols:3%} {{n}} {% endtablerow %}",
     *                          'collections' => {'frontpage' => [1,2,3,4,5,6]})
     *
     * end
     */
    @Test
    public void quotedFragmentTest() throws RecognitionException {

        assertThat(
                Template.parse("{% tablerow n in collections.frontpage cols:3%} {{n}} {% endtablerow %}")
                        .render("{ \"collections\" : { \"frontpage\" : [1,2,3,4,5,6] } }"),
                is("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 4 </td><td class=\"col2\"> 5 </td><td class=\"col3\"> 6 </td></tr>\n"));

        assertThat(
                Template.parse("{% tablerow n in collections['frontpage'] cols:3%} {{n}} {% endtablerow %}")
                        .render("{ \"collections\" : { \"frontpage\" : [1,2,3,4,5,6] } }"),
                is("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 4 </td><td class=\"col2\"> 5 </td><td class=\"col3\"> 6 </td></tr>\n"));
    }

    /*
     * def test_enumerable_drop
     *   assert_template_result("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 4 </td><td class=\"col2\"> 5 </td><td class=\"col3\"> 6 </td></tr>\n",
     *                          '{% tablerow n in numbers cols:3%} {{n}} {% endtablerow %}',
     *                          'numbers' => ArrayDrop.new([1,2,3,4,5,6]))
     * end
     */
    @Test
    public void enumerableDropTest() throws RecognitionException {

        assertThat(
                Template.parse("{% tablerow n in numbers cols:3%} {{n}} {% endtablerow %}")
                        .render("{ \"numbers\" : [1,2,3,4,5,6] }"),
                is("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 4 </td><td class=\"col2\"> 5 </td><td class=\"col3\"> 6 </td></tr>\n"));
    }

    /*
     * def test_offset_and_limit
     *   assert_template_result("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 4 </td><td class=\"col2\"> 5 </td><td class=\"col3\"> 6 </td></tr>\n",
     *                          '{% tablerow n in numbers cols:3 offset:1 limit:6%} {{n}} {% endtablerow %}',
     *                          'numbers' => [0,1,2,3,4,5,6,7])
     * end
     */
    @Test
    public void offsetAndLimitTest() throws RecognitionException {

        assertThat(
                Template.parse("{% tablerow n in numbers cols:3 offset:1 limit:6%} {{n}} {% endtablerow %}")
                        .render("{ \"numbers\" : [1,2,3,4,5,6] }"),
                is("<tr class=\"row1\">\n<td class=\"col1\"> 1 </td><td class=\"col2\"> 2 </td><td class=\"col3\"> 3 </td></tr>\n<tr class=\"row2\"><td class=\"col1\"> 4 </td><td class=\"col2\"> 5 </td><td class=\"col3\"> 6 </td></tr>\n"));
    }
}
