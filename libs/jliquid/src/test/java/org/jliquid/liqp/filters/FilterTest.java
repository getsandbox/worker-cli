package org.jliquid.liqp.filters;

import org.jliquid.liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FilterTest {

    @Test
    public void testCustomFilter() throws RecognitionException {

        Filter.registerFilter(new Filter("textilize") {
            @Override
            public Object apply(Object value, Object... params) {
                String s = super.asString(value).trim();
                return "<b>" + s.substring(1, s.length() - 1) + "</b>";
            }
        });

        Template template = Template.parse("{{ '*hi*' | textilize }}");
        String rendered = String.valueOf(template.render());

        assertThat(rendered, is("<b>hi</b>"));
    }
}
