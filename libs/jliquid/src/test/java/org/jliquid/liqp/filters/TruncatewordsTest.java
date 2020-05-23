package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TruncatewordsTest {

    @Test
    public void applyTest() throws RecognitionException {

        String json = "{ \"txt\" : \"a        b c d e f g h i j a b c d e f g h i j\" }";

        String[][] tests = {
            {"{{ nil | truncatewords }}", ""},
            {"{{ txt | truncatewords }}", "a b c d e f g h i j a b c d e..."},
            {"{{ txt | truncatewords: 5 }}", "a b c d e..."},
            {"{{ txt | truncatewords: 5, '???' }}", "a b c d e???"},
            {"{{ txt | truncatewords: 500, '???' }}", "a        b c d e f g h i j a b c d e f g h i j"},
            {"{{ txt | truncatewords: 2, '===' }}", "a b==="},
            {"{{ txt | truncatewords: 19, '===' }}", "a b c d e f g h i j a b c d e f g h i==="},
            {"{{ txt | truncatewords: 20, '===' }}", "a        b c d e f g h i j a b c d e f g h i j"},
            {"{{ txt | truncatewords: 21, '===' }}", "a        b c d e f g h i j a b c d e f g h i j"},};

        for (String[] test : tests) {

            Template template = Template.parse(test[0]);
            String rendered = String.valueOf(template.render(json));

            assertThat(rendered, is(test[1]));
        }
    }

    /*
     * def test_truncatewords
     *   assert_equal 'one two three', @filters.truncatewords('one two three', 4)
     *   assert_equal 'one two...', @filters.truncatewords('one two three', 2)
     *   assert_equal 'one two three', @filters.truncatewords('one two three')
     *   assert_equal 'Two small (13&#8221; x 5.5&#8221; x 10&#8221; high) baskets fit inside one large basket (13&#8221;...',
     *                 @filters.truncatewords('Two small (13&#8221; x 5.5&#8221; x 10&#8221; high) baskets fit inside one large basket (13&#8221; x 16&#8221; x 10.5&#8221; high) with cover.', 15)
     * end
     */
    @Test
    public void applyOriginalTest() {

        final Filter filter = Filter.getFilter("truncatewords");

        assertThat(filter.apply("one two three", 4), is((Object) "one two three"));
        assertThat(filter.apply("one two three", 2), is((Object) "one two..."));
        assertThat(filter.apply("one two three", 3), is((Object) "one two three"));
        assertThat(filter.apply("Two small (13&#8221; x 5.5&#8221; x 10&#8221; high) baskets fit inside one large basket (13&#8221; x 16&#8221; x 10.5&#8221; high) with cover.", 15),
                is((Object) "Two small (13&#8221; x 5.5&#8221; x 10&#8221; high) baskets fit inside one large basket (13&#8221;..."));
    }
}
