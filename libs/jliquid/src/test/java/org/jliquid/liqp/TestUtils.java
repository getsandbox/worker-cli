package org.jliquid.liqp;

import java.lang.reflect.Method;
import org.jliquid.liqp.nodes.LNode;
import org.jliquid.liqp.nodes.LiquidWalker;
import org.jliquid.liqp.parser.LiquidLexer;
import org.jliquid.liqp.parser.LiquidParser;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

public final class TestUtils {

    private TestUtils() {
        // no need to instantiate this class
    }

    /**
     * Parses the input `source` and invokes the `rule` and returns this LNode.
     *
     * @param source the input source to be parsed.
     * @param rule the rule name (method name) to be invoked
     * @return
     * @throws Exception
     */
    public static LNode getNode(String source, String rule) throws Exception {

        LiquidLexer lexer = new LiquidLexer(new ANTLRStringStream("{{" + source + "}}"));
        LiquidParser parser = new LiquidParser(new CommonTokenStream(lexer));

        CommonTree root = (CommonTree) parser.parse().getTree();
        CommonTree child = (CommonTree) root.getChild(0).getChild(0);

        LiquidWalker walker = new LiquidWalker(new CommonTreeNodeStream(child));

        Method method = walker.getClass().getMethod(rule);

        return (LNode) method.invoke(walker);
    }
}
