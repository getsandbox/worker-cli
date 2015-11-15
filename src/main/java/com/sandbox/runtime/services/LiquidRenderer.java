package com.sandbox.runtime.services;

import com.sandbox.runtime.js.utils.INashornUtils;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.jliquid.liqp.Template;
import org.jliquid.liqp.nodes.LNode;
import org.jliquid.liqp.tags.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickhoughton on 4/08/2014.
 */
public class LiquidRenderer {

    public LiquidRenderer() {
        Tag.registerTag(new Tag("include") {
            @Override
            public Object render(Map<String, Object> context, LNode... nodes) {
                String fileNameWithoutExt = "templates/" + super.asString(nodes[0].render(context));
                if(!fileNameWithoutExt.endsWith(".liquid")) fileNameWithoutExt += ".liquid";

                INashornUtils nashornUtils = (INashornUtils) context.get("__nashornUtils");
                String includeTemplate = nashornUtils.readFile(fileNameWithoutExt);
                if(includeTemplate == null) throw new LiquidRendererException("Can't find template: " + fileNameWithoutExt);
                if(Thread.currentThread().getStackTrace().length > 150) throw new LiquidRendererException("Stack level too deep! Possible recursive template.");
                Template include = Template.parse(includeTemplate);

                // check if there's a optional "with expression"
                if (nodes.length > 1) {
                    Object value = nodes[1].render(context);
                    context.put(fileNameWithoutExt, value);
                }

                return include.render(context);

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

    public Map prepareValues(Map<String, Object> values) {
        return processRenderMap(values);
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

    //helper functions
    private Map processRenderMap(Map<String, Object> locals) {
        if (locals == null) return null;

        for (String key : locals.keySet()) {
            Object value = locals.get(key);
            locals.put(key, processRenderValue(value));
        }

        return locals;
    }

    private List processRenderCollection(Collection<Object> values) {
        if (values == null) return null;

        List<Object> convertedValues = new ArrayList<>();
        for (Object value : values) {
            convertedValues.add(processRenderValue(value));
        }

        return convertedValues;
    }

    private Object processRenderValue(Object value) {
        if (value instanceof ScriptObjectMirror) {
            ScriptObjectMirror objectValue = (ScriptObjectMirror) value;

            if (objectValue.isArray()) {
                return processRenderCollection(objectValue.values());
            } else {
                return processRenderMap(objectValue);
            }

        } else if (value instanceof ScriptObject) {
            ScriptObject objectValue = (ScriptObject) value;

            if (objectValue.isArray()) {
                return processRenderCollection(objectValue.values());

            } else if (objectValue.getMap() != null) {
                Map<String, Object> objectMap = new HashMap<>();
                for (Object key : objectValue.keySet()) objectMap.put((String) key, objectValue.get(key));
                return processRenderMap(objectMap);

            }
        } else if (value instanceof Collection) {
            return processRenderCollection((Collection<Object>) value);

        } else if (value instanceof Map) {
            return processRenderMap((Map<String, Object>) value);

        }

        return value;

    }

}
