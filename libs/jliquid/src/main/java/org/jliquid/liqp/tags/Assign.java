package org.jliquid.liqp.tags;

import org.jliquid.liqp.nodes.FilterNode;
import org.jliquid.liqp.nodes.LNode;

import java.util.Map;

class Assign extends Tag {

    /*
     * Assigns some value to a variable
     */
    @Override
    public Object render(Map<String, Object> context, LNode... nodes) {

        String id = String.valueOf(nodes[0].render(context));

        FilterNode filter = null;
        LNode expression;

        if (nodes.length >= 3) {
            filter = (FilterNode) nodes[1];
            expression = nodes[2];
        } else {
            expression = nodes[1];
        }

        Object value = expression.render(context);

        if (filter != null) {
            value = filter.apply(value, context);
        }

        context.put(id, value);

        return "";
    }
}
