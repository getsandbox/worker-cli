package com.sandbox.worker.core.services;


import com.sandbox.worker.core.js.ScriptFunctions;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jliquid.liqp.LimitedStringBuilder;
import org.jliquid.liqp.Template;
import org.jliquid.liqp.nodes.LNode;
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

        Tag.registerTag(new Tag("include") {
            @Override
            public Object render(Map<String, Object> context, LNode... nodes) {
                int recursionDepth = (int) context.getOrDefault("__includeDepth", 0);
                int depth = recursionDepth + 1;
                if(depth > 150) throw new LiquidRendererException("Stack level too deep! Possible recursive template.");
                context.put("__includeDepth", depth);

                LOG.debug("includeRender start");
                long start = System.currentTimeMillis();

                String fileNameWithoutExt = "templates/" + super.asString(nodes[0].render(context));
                if(!fileNameWithoutExt.endsWith(".liquid")) fileNameWithoutExt += ".liquid";
                String cacheKey = System.identityHashCode(context.get("__service")) + "-" + fileNameWithoutExt;

                // check if there's a optional "with expression"
                if (nodes.length > 1) {
                    Object value = nodes[1].render(context);
                    context.put(fileNameWithoutExt, value);
                }

                SoftReference<Template> includeRef = cachedTemplates.get(cacheKey);
                try {
                    Template include;
                    if(includeRef != null && includeRef.get() != null) {
                        include = includeRef.get();
                        if (LOG.isDebugEnabled()) LOG.debug("includeRender cached end: {}ms key: '{}'", System.currentTimeMillis() - start, cacheKey);
                        return include.render(context);
                    } else {
                        ScriptFunctions scriptFunctions = (ScriptFunctions) context.get("__service");
                        String includeTemplate = scriptFunctions.readFile(fileNameWithoutExt);
                        if(includeTemplate == null) throw new LiquidRendererException("Can't find template: " + fileNameWithoutExt);
                        include = Template.parse(includeTemplate);
                        cachedTemplates.put(cacheKey, new SoftReference<>(include));

                        if (LOG.isDebugEnabled()) LOG.debug("includeRender full end: {}ms", System.currentTimeMillis() - start);
                        return include.render(context);
                    }
                } finally {
                    recursionDepth = (int) context.getOrDefault("__includeDepth", 0);
                    depth = recursionDepth - 1;
                    context.put("__includeDepth", depth);
                }
            }
        });
    }

    public String render(String templateData, Map<String, Object> parameters) {
        //compile template out of raw string, this could be cached for multiple calls.
        Template compiledTemplate = Template.parse(prepareTemplate(templateData));

        //render vars into template
        String rendered = compiledTemplate.render(parameters);
        return rendered;
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
