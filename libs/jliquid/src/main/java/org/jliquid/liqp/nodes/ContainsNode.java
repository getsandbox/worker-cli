package org.jliquid.liqp.nodes;

import java.util.Arrays;
import java.util.Map;
import org.jliquid.liqp.LValue;

class ContainsNode extends LValue implements LNode {

    private LNode lhs;
    private LNode rhs;

    public ContainsNode(LNode lhs, LNode rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public Object render(Map<String, Object> context) {

        Object collection = lhs.render(context);
        Object needle = rhs.render(context);

        if (super.isArray(collection)) {
            Object[] array = super.asArray(collection);
            return Arrays.asList(array).contains(needle);
        }

        if (super.isString(collection)) {
            return super.asString(collection).contains(super.asString(needle));
        }

        return false;
    }
}
