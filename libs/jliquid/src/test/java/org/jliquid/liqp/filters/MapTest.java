package org.jliquid.liqp.filters;

import java.util.HashMap;
import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MapTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{\"products\" : [\n"
                + "  {\"name\" : \"C\", \"price\" : 1}, \n"
                + "  {\"name\" : \"A\", \"price\" : 3},\n"
                + "  {\"name\" : \"B\", \"price\" : 2}\n"
                + "]}";

        String[][] tests = {
            {"{{ mu | map:'name' }}", ""},
            {"{{ products | map:'XYZ' }}", ""},
            {"{{ products | map:'XYZ' | sort | join }}", ""},
            {"{{ products | map:'name' | sort | join }}", "A B C"},
            {"{{ products | map:'price' | sort | join:'=' }}", "1=2=3"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_map
     *   assert_equal [1,2,3,4], @filters.map([{"a" => 1}, {"a" => 2}, {"a" => 3}, {"a" => 4}], 'a')
     *   assert_template_result 'abc', "{{ ary | map:'foo' | map:'bar' }}",
     *     'ary' => [{'foo' => {'bar' => 'a'}}, {'foo' => {'bar' => 'b'}}, {'foo' => {'bar' => 'c'}}]
     * end
     */
    @Test
    public void applyOriginalTest() {

        Filter filter = Filter.getFilter("map");

        Object[] rendered = (Object[]) filter.apply(
                new HashMap[]{
            new HashMap<String, Integer>() {
                {
                    put("a", 1);
                }
            },
            new HashMap<String, Integer>() {
                {
                    put("a", 2);
                }
            },
            new HashMap<String, Integer>() {
                {
                    put("a", 3);
                }
            },
            new HashMap<String, Integer>() {
                {
                    put("a", 4);
                }
            },},
                "a");

        Object[] expected = {1, 2, 3, 4};

        assertThat(rendered, is(expected));

        final String json = "{\"ary\":[{\"foo\":{\"bar\":\"a\"}}, {\"foo\":{\"bar\":\"b\"}}, {\"foo\":{\"bar\":\"c\"}}]}";

        assertThat(Template.parse("{{ ary | map:'foo' | map:'bar' }}").render(json), is("abc"));
    }
}
