package org.jliquid.liqp.tags;

import org.jliquid.liqp.nodes.LNode;

import java.util.Map;

class Capture extends Tag {

    /*
     * Block tag that captures text into a variable
     */
    @Override
    public Object render(Map<String, Object> context, LNode... nodes) {

        String id = super.asString(nodes[0].render(context));

        LNode block = nodes[1];

        context.put(id, block.render(context));

        return null;
    }
}
