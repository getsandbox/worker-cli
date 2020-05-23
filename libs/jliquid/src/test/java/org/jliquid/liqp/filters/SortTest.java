package org.jliquid.liqp.filters;

import java.util.HashMap;
import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SortTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json =
            "{" +
                " \"words\"   : [\"2\", \"13\", \"1\"], " +
                " \"numbers\" : [2, 13, 1] " +
            "}";

        String[][] tests = {
                {"{{ x | sort }}", ""},
                {"{{ words | sort }}", "1132"},
                {"{{ numbers | sort }}", "1213"},
                {"{{ numbers | sort | last }}", "13"},
                {"{{ numbers | sort | first }}", "1"},
        };

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_sort
     *   assert_equal [1,2,3,4], @filters.sort([4,3,2,1])
     *   assert_equal [{"a" => 1}, {"a" => 2}, {"a" => 3}, {"a" => 4}], @filters.sort([{"a" => 4}, {"a" => 3}, {"a" => 1}, {"a" => 2}], "a")
     * end
     */
    @Test
    public void applyOriginalTest() {

        Filter filter = Filter.getFilter("sort");

        assertThat(filter.apply(new Integer[]{4,3,2,1}), is((Object)new Integer[]{1,2,3,4}));

        java.util.Map[] unsorted = new java.util.Map[]{
                new HashMap<String, Integer>(){{ put("a", 4); }},
                new HashMap<String, Integer>(){{ put("a", 3); }},
                new HashMap<String, Integer>(){{ put("a", 2); }},
                new HashMap<String, Integer>(){{ put("a", 1); }}
        };

        java.util.Map[] sorted = (Sort.SortableMap[])filter.apply(unsorted, "a");

        java.util.Map[] expected = new java.util.Map[]{
                new HashMap<String, Integer>(){{ put("a", 1); }},
                new HashMap<String, Integer>(){{ put("a", 2); }},
                new HashMap<String, Integer>(){{ put("a", 3); }},
                new HashMap<String, Integer>(){{ put("a", 4); }}
        };

        assertThat(sorted, is(expected));
    }
}
