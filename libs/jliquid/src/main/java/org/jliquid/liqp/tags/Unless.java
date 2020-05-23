package org.jliquid.liqp.tags;

import org.jliquid.liqp.nodes.LNode;

import java.util.Map;

class Unless extends Tag {

    /*
     * Mirror of if statement
     */
    @Override
    public Object render(Map<String, Object> context, LNode... nodes) {

        for (int i = 0; i < nodes.length - 1; i += 2) {

            Object exprNodeValue = nodes[i].render(context);
            LNode blockNode = nodes[i + 1];

            if (!super.asBoolean(exprNodeValue)) {
                return blockNode.render(context);
            }
        }

        return "";
    }
}
