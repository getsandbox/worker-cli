package org.jliquid.liqp.tags;

import org.jliquid.liqp.Template;
import org.jliquid.liqp.nodes.LNode;

import java.io.File;
import java.net.URL;
import java.util.Map;

class Include extends Tag {

    public static String snippetsFolder = "snippets";
    public static String extension = ".liquid";

    @Override
    public Object render(Map<String, Object> context, LNode... nodes) {

        try {
            String fileNameWithoutExt = super.asString(nodes[0].render(context));

            
//            ClassLoader loader = Thread.currentThread().getContextClassLoader();
//            URL url = loader.getResource(snippetsFolder + "/" + fileNameWithoutExt + extension);
//
//            Template include = Template.parse(new File(url.toURI()));
//
//            // check if there's a optional "with expression"
//            if (nodes.length > 1) {
//                Object value = nodes[1].render(context);
//                context.put(fileNameWithoutExt, value);
//            }
//
//            return include.render(context);

            //TODO Support includes properly
            return "";

        } catch (Exception e) {
            return "";
        }
    }
}
    