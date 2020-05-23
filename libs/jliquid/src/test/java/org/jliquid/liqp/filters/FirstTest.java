package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FirstTest {

    @Test
    public void applyTest() throws RecognitionException {

        Template template = Template.parse("{{values | first}}");

        String rendered = String.valueOf(template.render("{\"values\" : [\"Mu\", \"foo\", \"bar\"]}"));

        assertThat(rendered, is("Mu"));
    }

    /*
     * def test_first_last
     *   assert_equal 1, @filters.first([1,2,3])
     *   assert_equal 3, @filters.last([1,2,3])
     *   assert_equal nil, @filters.first([])
     *   assert_equal nil, @filters.last([])
     * end
     */
    @Test
    public void applyOriginalTest() {

        Filter filter = Filter.getFilter("first");

        assertThat(filter.apply(new Integer[]{1, 2, 3}), is((Object) "1"));
        assertThat(filter.apply(new Integer[]{}), is((Object) null));
    }
}
