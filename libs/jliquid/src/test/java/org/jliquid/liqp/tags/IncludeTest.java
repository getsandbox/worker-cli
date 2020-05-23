package org.jliquid.liqp.tags;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IncludeTest {

//    @Test
    public void renderTest() throws RecognitionException {

        String source =
                "{% assign shape = 'circle' %}\n" +
                "{% include 'color' %}\n" +
                "{% include 'color' with 'red' %}\n" +
                "{% include 'color' with 'blue' %}\n" +
                "{% assign shape = 'square' %}\n" +
                "{% include 'color' with 'red' %}";

        Template template = Template.parse(source);

        String rendered = template.render();

        assertThat(rendered, is("\n" +
                "color: ''\n" +
                "shape: 'circle'\n" +
                "color: 'red'\n" +
                "shape: 'circle'\n" +
                "color: 'blue'\n" +
                "shape: 'circle'\n" +
                "\n" +
                "color: 'red'\n" +
                "shape: 'square'"));
    }
}
