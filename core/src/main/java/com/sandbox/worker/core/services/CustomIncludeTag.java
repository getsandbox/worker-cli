package com.sandbox.worker.core.services;

import com.sandbox.worker.core.js.ScriptFunctions;

import java.lang.ref.SoftReference;
import java.util.Map;
import org.jliquid.liqp.Template;
import org.jliquid.liqp.nodes.LNode;
import org.jliquid.liqp.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CustomIncludeTag extends Tag {

    private static final Logger LOG = LoggerFactory.getLogger(CustomIncludeTag.class);
    private final Map<String, SoftReference<Template>> cachedTemplates;

    public CustomIncludeTag(Map<String, SoftReference<Template>> cachedTemplates) {
        super("include");
        this.cachedTemplates = cachedTemplates;
    }

    @Override
    public Object render(Map<String, Object> context, LNode... nodes) {
        int recursionDepth = (int) context.getOrDefault("__includeDepth", 0);
        int depth = recursionDepth + 1;
        if(depth > 150) throw new LiquidRendererException("Stack level too deep! Possible recursive template.");
        context.put("__includeDepth", depth);

        String templateNameWithoutExt = (String) nodes[0].render(context);
        String cacheKey = "i-" + System.identityHashCode(context.get("__service")) + "-" + templateNameWithoutExt;

        // check if there's a optional "with expression"
        if (nodes.length > 1) {
            Object value = nodes[1].render(context);
            context.put(templateNameWithoutExt, value);
        }

        SoftReference<Template> includeRef = cachedTemplates.get(cacheKey);
        try {
            Template include;
            if(includeRef != null && includeRef.get() != null) {
                include = includeRef.get();
                return include.render(context);
            } else {
                ScriptFunctions scriptFunctions = (ScriptFunctions) context.get("__service");
                if(!templateNameWithoutExt.endsWith(".liquid")) templateNameWithoutExt += ".liquid";
                String includeTemplate = scriptFunctions.readFile("templates/" + templateNameWithoutExt);
                if(includeTemplate == null) throw new LiquidRendererException("Can't find template: templates/" + templateNameWithoutExt);
                include = Template.parse(includeTemplate);
                cachedTemplates.put(cacheKey, new SoftReference<>(include));

                LOG.info("Parsing template for include(...) with key: {} cache size: {}", cacheKey, cachedTemplates.size());
                return include.render(context);
            }
        } finally {
            recursionDepth = (int) context.getOrDefault("__includeDepth", 0);
            depth = recursionDepth - 1;
            context.put("__includeDepth", depth);
        }
    }
}
