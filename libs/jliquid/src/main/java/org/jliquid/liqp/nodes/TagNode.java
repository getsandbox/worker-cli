package org.jliquid.liqp.nodes;

import org.jliquid.liqp.tags.Tag;

import java.util.Map;

class TagNode implements LNode {

    private Tag tag;
    private LNode[] tokens;

    public TagNode(String name, LNode... tokens) {
        this.tag = Tag.getTag(name);
        this.tokens = tokens;
    }

    @Override
    public Object render(Map<String, Object> context) {

        return tag.render(context, tokens);
    }
}
