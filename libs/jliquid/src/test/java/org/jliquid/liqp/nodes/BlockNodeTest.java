package org.jliquid.liqp.nodes;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.jliquid.liqp.LimitedStringBuilder;
import org.jliquid.liqp.Template;
import org.jliquid.liqp.parser.LiquidLexer;
import org.jliquid.liqp.tags.Tag;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BlockNodeTest {

    private static String getChildText(CommonTree tree) {

        LimitedStringBuilder builder = new LimitedStringBuilder();

        for (int i = 0; i < tree.getChildCount(); i++) {
            builder.append(tree.getChild(i).getText());
        }

        return builder.toString();
    }

    /*
     * def test_blankspace
     *   template = Liquid::Template.parse("  ")
     *   assert_equal ["  "], template.root.nodelist
     * end
     */
    @Test
    public void blankSpaceTest() throws RecognitionException {

        CommonTree root = Template.parse("  ").getAST();

        assertThat(getChildText(root), is("  "));
    }

    /*
     * def test_variable_beginning
     *   template = Liquid::Template.parse("{{funk}}  ")
     *   assert_equal 2, template.root.nodelist.size
     *   assert_equal Variable, template.root.nodelist[0].class
     *   assert_equal String, template.root.nodelist[1].class
     * end
     */
    @Test
    public void variableBeginningTest() throws RecognitionException {

        CommonTree root = Template.parse("{{funk}}  ").getAST();

        assertThat(root.getChildCount(), is(2));

        assertThat(root.getChild(0).getChild(0).getType(), is(LiquidLexer.LOOKUP));
        assertThat(root.getChild(1).getType(), is(LiquidLexer.PLAIN));
    }

    /*
     * def test_variable_end
     *   template = Liquid::Template.parse("  {{funk}}")
     *   assert_equal 2, template.root.nodelist.size
     *   assert_equal String, template.root.nodelist[0].class
     *   assert_equal Variable, template.root.nodelist[1].class
     * end
     */
    @Test
    public void variableEndTest() throws RecognitionException {

        CommonTree root = Template.parse("  {{funk}}").getAST();

        assertThat(root.getChildCount(), is(2));

        assertThat(root.getChild(0).getType(), is(LiquidLexer.PLAIN));
        assertThat(root.getChild(1).getChild(0).getType(), is(LiquidLexer.LOOKUP));
    }

    /*
     * def test_variable_middle
     *   template = Liquid::Template.parse("  {{funk}}  ")
     *   assert_equal 3, template.root.nodelist.size
     *   assert_equal String, template.root.nodelist[0].class
     *   assert_equal Variable, template.root.nodelist[1].class
     *   assert_equal String, template.root.nodelist[2].class
     * end
     */
    @Test
    public void variableMiddleTest() throws RecognitionException {

        CommonTree root = Template.parse("  {{funk}}  ").getAST();

        assertThat(root.getChildCount(), is(3));

        assertThat(root.getChild(0).getType(), is(LiquidLexer.PLAIN));
        assertThat(root.getChild(1).getChild(0).getType(), is(LiquidLexer.LOOKUP));
        assertThat(root.getChild(2).getType(), is(LiquidLexer.PLAIN));
    }

    /*
     * def test_variable_many_embedded_fragments
     *   template = Liquid::Template.parse("  {{funk}} {{so}} {{brother}} ")
     *   assert_equal 7, template.root.nodelist.size
     *   assert_equal [String, Variable, String, Variable, String, Variable, String],
     *                block_types(template.root.nodelist)
     * end
     */
    @Test
    public void variableManyEmbeddedFragmentsTest() throws RecognitionException {

        CommonTree root = Template.parse("  {{funk}} {{so}} {{brother}} ").getAST();

        assertThat(root.getChildCount(), is(7));

        assertThat(root.getChild(0).getType(), is(LiquidLexer.PLAIN));
        assertThat(root.getChild(1).getChild(0).getType(), is(LiquidLexer.LOOKUP));
        assertThat(root.getChild(2).getType(), is(LiquidLexer.PLAIN));
        assertThat(root.getChild(3).getChild(0).getType(), is(LiquidLexer.LOOKUP));
        assertThat(root.getChild(4).getType(), is(LiquidLexer.PLAIN));
        assertThat(root.getChild(5).getChild(0).getType(), is(LiquidLexer.LOOKUP));
        assertThat(root.getChild(6).getType(), is(LiquidLexer.PLAIN));
    }

    /*
     * def test_with_block
     *   template = Liquid::Template.parse("  {% comment %} {% endcomment %} ")
     *   assert_equal [String, Comment, String], block_types(template.root.nodelist)
     *   assert_equal 3, template.root.nodelist.size
     * end
     */
    @Test
    public void blockTest() throws RecognitionException {

        CommonTree root = Template.parse("  {% comment %} {% endcomment %} ").getAST();

        assertThat(root.getChildCount(), is(3));

        assertThat(root.getChild(0).getType(), is(LiquidLexer.PLAIN));
        assertThat(root.getChild(1).getType(), is(LiquidLexer.COMMENT));
        assertThat(root.getChild(2).getType(), is(LiquidLexer.PLAIN));
    }

    /*
     * def test_with_custom_tag
     *   Liquid::Template.register_tag("testtag", Block)
     *
     *   assert_nothing_thrown do
     *     template = Liquid::Template.parse( "{% testtag %} {% endtesttag %}")
     *   end
     * end
     */
    @Test
    public void customTagTest() throws RecognitionException {

        Tag.registerTag(new Tag("testtag") {
            @Override
            public Object render(Map<String, Object> context, LNode... nodes) {
                return null;
            }
        });

        Template.parse("{% testtag %} {% endtesttag %}").render();
    }
}
