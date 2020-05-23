package org.jliquid.liqp.tags;

import org.jliquid.liqp.Template;
import org.jliquid.liqp.nodes.LNode;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TagTest {

    @Test
    public void testCustomTag() throws RecognitionException {

        Tag.registerTag(new Tag("twice") {
            @Override
            public Object render(Map<String, Object> context, LNode... nodes) {
                Double number = super.asNumber(nodes[0].render(context)).doubleValue();
                return number * 2;
            }
        });

        Template template = Template.parse("{% twice 10 %}");
        String rendered = String.valueOf(template.render());

        assertThat(rendered, is("20.0"));
    }

    @Test
    public void testCustomTagBlock() throws RecognitionException {

        Tag.registerTag(new Tag("twice") {
            @Override
            public Object render(Map<String, Object> context, LNode... nodes) {
                LNode blockNode = nodes[nodes.length - 1];
                String blockValue = super.asString(blockNode.render(context));
                return blockValue + " " + blockValue;
            }
        });

        Template template = Template.parse("{% twice %}abc{% endtwice %}");
        String rendered = String.valueOf(template.render());

        assertThat(rendered, is("abc abc"));
    }

    @Test
    public void breakTest() throws RecognitionException {

        final String context = "{\"array\":[11,22,33,44,55]}";

        final String markup = "{% for item in array %}"
                + "{% if item > 35 %}{% break %}{% endif %}"
                + "{{ item }}"
                + "{% endfor %}";

        assertThat(Template.parse(markup).render(context), is("112233"));
    }

    /*
     * def test_break_with_no_block
     *   assigns = {'i' => 1}
     *   markup = '{% break %}'
     *   expected = ''
     *
     *   assert_template_result(expected, markup, assigns)
     * end
     */
    @Test
    public void breakWithNoBlockTest() throws RecognitionException {

        assertThat(Template.parse("{% break %}").render(), is(""));
    }

    @Test
    public void continueTest() throws RecognitionException {

        final String context = "{\"array\":[11,22,33,44,55]}";

        final String markup = "{% for item in array %}"
                + "{% if item < 35 %}{% continue %}{% endif %}"
                + "{{ item }}"
                + "{% endfor %}";

        assertThat(Template.parse(markup).render(context), is("4455"));
    }

    /*
     * def test_continue_with_no_block
     *   assigns = {'i' => 1}
     *   markup = '{% continue %}'
     *   expected = ''
     *
     *   assert_template_result(expected, markup, assigns)
     * end
     */
    @Test
    public void continueWithNoBlockTest() throws RecognitionException {

        assertThat(Template.parse("{% continue %}").render(), is(""));
    }

    /*
     * def test_no_transform
     *   assert_template_result('this text should come out of the template without change...',
     *                          'this text should come out of the template without change...')
     *
     *   assert_template_result('blah','blah')
     *   assert_template_result('<blah>','<blah>')
     *   assert_template_result('|,.:','|,.:')
     *   assert_template_result('','')
     *
     *   text = %|this shouldnt see any transformation either but has multiple lines
     *             as you can clearly see here ...|
     *   assert_template_result(text,text)
     * end
     */
    @Test
    public void no_transformTest() throws RecognitionException {

        assertThat(Template.parse("this text should come out of the template without change...").render(),
                is("this text should come out of the template without change..."));

        assertThat(Template.parse("blah").render(), is("blah"));
        assertThat(Template.parse("<blah>").render(), is("<blah>"));
        assertThat(Template.parse("|,.:").render(), is("|,.:"));
        assertThat(Template.parse("").render(), is(""));

        String text = "this shouldnt see any transformation either but has multiple lines\n as you can clearly see here ...";
        assertThat(Template.parse(text).render(), is(text));
    }
}
