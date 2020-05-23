package org.jliquid.liqp.tags;

import org.jliquid.liqp.nodes.LNode;

import java.util.Map;

class Comment extends Tag {

    /*
     * Block tag, comments out the text in the block
     */
    @Override
    public Object render(Map<String, Object> context, LNode... nodes) {
        return "";
    }
}
