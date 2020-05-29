package com.sandbox.worker.core.services;


import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jliquid.liqp.LimitedStringBuilder;
import org.jliquid.liqp.Template;
import org.jliquid.liqp.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiquidRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(LiquidRenderer.class);
    private Map<String, SoftReference<Template>> cachedTemplates = new LinkedHashMap(0, .75F, true) {
        // This method is called just after a new entry has been added
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 50;
        }
    };

    public LiquidRenderer(int maximumRenderSize) {
        LimitedStringBuilder.limitInBytes = maximumRenderSize;
        Tag.registerTag(new CustomIncludeTag(cachedTemplates));
    }

    public String render(String templateKey, Supplier<String> templateData, Map<String, Object> parameters) {
        SoftReference<Template> templateRef = cachedTemplates.get(templateKey);
        if(templateRef != null && templateRef.get() != null) {
            return templateRef.get().render(parameters);
        } else {
            Template compiledTemplate = Template.parse(prepareTemplate(templateData.get()));
            LOG.info("Parsing template for render(...) with key: {} cache size: {}", templateKey, cachedTemplates.size());
            cachedTemplates.put(templateKey, new SoftReference<>(compiledTemplate));
            return compiledTemplate.render(parameters);
        }
    }

    //this cleans up a template before we parse/use it to render, because of the liquid syntax we often have lines just for syntacic reasons (like for/endfor) that
    //leave new line characters in the output, in a parsed output like json or xml that doesn't matter, for a human readable output this isn't great
    public String prepareTemplate(String template) {
        //matches with a end of line (incl whitespace) and a {% %} expression, replaces with just the expression
        //or matches an start of line with a {% %} expression, replaces with just expression
        Pattern pattern = Pattern.compile("^\\s*(\\{%.*%\\})\\s*[\\r\\n]|\\s*[\\r\\n](\\{%.*%\\})$", Pattern.MULTILINE);
        Matcher m = pattern.matcher(template);
        //replace the match with just the capture group, $1 is the first half $2 is second half, they are mutually exclusive so can put both in
        return m.replaceAll("$1$2");
    }

}
