package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JoinTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{ \"array\" : [\"x\", \"y\", \"z\"] }";

        String[][] tests = {
            {"{{ array | join }}", "x y z"},
            {"{{ array | join:'' }}", "xyz"},
            {"{{ array | join:'@@@' }}", "x@@@y@@@z"},
            {"{{ x | join:'@@@' }}", ""},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_join
     *   assert_equal '1 2 3 4', @filters.join([1,2,3,4])
     *   assert_equal '1 - 2 - 3 - 4', @filters.join([1,2,3,4], ' - ')
     * end
     */
    @Test
    public void applyOriginalTest() {

        Filter filter = Filter.getFilter("join");

        assertThat(filter.apply(new Integer[]{1, 2, 3, 4}), is((Object) "1 2 3 4"));
        assertThat(filter.apply(new Integer[]{1, 2, 3, 4}, " - "), is((Object) "1 - 2 - 3 - 4"));
    }
}
