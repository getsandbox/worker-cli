package org.jliquid.liqp.nodes;

import org.jliquid.liqp.LValue;

import java.util.Map;

class NEqNode implements LNode {

    private LNode lhs;
    private LNode rhs;

    public NEqNode(LNode lhs, LNode rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public Object render(Map<String, Object> context) {

        Object a = lhs.render(context);
        Object b = rhs.render(context);

        return !LValue.areEqual(a, b);
    }
}
