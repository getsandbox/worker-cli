package org.jliquid.liqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jliquid.liqp.nodes.LNode;
import org.jliquid.liqp.parser.LiquidLexer;
import org.jliquid.liqp.parser.LiquidParser;
import org.jliquid.liqp.nodes.LiquidWalker;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main class of this library. Use one of its static
 * <code>parse(...)</code> to get a hold of a reference.
 * <p/>
 * Also see: https://github.com/Shopify/liquid
 */
public class Template {

    /**
     * The root of the AST denoting the Liquid input source.
     */
    private final CommonTree root;

    /**
     * Creates a new Template instance from a given input.
     *
     * @param input the file holding the Liquid source.
     */
    private Template(String input) {

        LiquidLexer lexer = new LiquidLexer(new ANTLRStringStream(input));
        LiquidParser parser = new LiquidParser(new CommonTokenStream(lexer));

        try {
            root = (CommonTree) parser.parse().getTree();
        } catch (RecognitionException e) {
            throw new RuntimeException("could not parse input: " + input, e);
        }
    }

    /**
     * Creates a new Template instance from a given file.
     *
     * @param file the file holding the Liquid source.
     */
    private Template(File file) throws IOException {

        try {
            LiquidLexer lexer = new LiquidLexer(new ANTLRFileStream(file.getAbsolutePath()));
            LiquidParser parser = new LiquidParser(new CommonTokenStream(lexer));
            root = (CommonTree) parser.parse().getTree();
        } catch (RecognitionException e) {
            throw new RuntimeException("could not parse input from " + file, e);
        }
    }

    /**
     * Returns the root of the AST of the parsed input.
     *
     * @return the root of the AST of the parsed input.
     */
    public CommonTree getAST() {
        return root;
    }

    /**
     * Returns a new Template instance from a given input string.
     *
     * @param input the input string holding the Liquid source.
     *
     * @return a new Template instance from a given input string.
     */
    public static Template parse(String input) {
        return new Template(input);
    }

    /**
     * Returns a new Template instance from a given input file.
     *
     * @param file the input file holding the Liquid source.
     *
     * @return a new Template instance from a given input file.
     */
    public static Template parse(File file) throws IOException {
        return new Template(file);
    }

    /**
     * Renders the template.
     *
     * @param jsonMap a JSON-map denoting the (possibly nested) variables that
     * can be used in this Template.
     *
     * @return a string denoting the rendered template.
     */
    @SuppressWarnings("unchecked")
    public String render(String jsonMap) {

        Map<String, Object> map;

        try {
            map = new ObjectMapper().readValue(jsonMap, HashMap.class);
        } catch (Exception e) {
            throw new RuntimeException("invalid json map: '" + jsonMap + "'", e);
        }

        return render(map);
    }

    /**
     * Renders the template.
     *
     * @param context an array denoting key-value pairs where the uneven numbers
     * (even indexes) should be Strings. If the length of this array is uneven,
     * the last key (without the value) gets `null` attached to it. Note that a
     * call to this method with a single String as parameter, will be handled by
     * `render(String jsonMap)` instead.
     *
     * @return a string denoting the rendered template.
     */
    public String render(Object... context) {

        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0; i < context.length - 1; i++) {

            Object key = context[i];

            if (key.getClass() != String.class) {
                throw new RuntimeException("illegal key: " + String.valueOf(key)
                        + " (" + key.getClass().getName() + "). Must be a String.");
            }

            Object value = context[i + 1];

            map.put((String) key, value);
        }

        return render(map);
    }

    /**
     * Renders the template.
     *
     * @param context a Map denoting the (possibly nested) variables that can be
     * used in this Template.
     *
     * @return a string denoting the rendered template.
     */
    public String render(Map<String, Object> context) {

        LiquidWalker walker = new LiquidWalker(new CommonTreeNodeStream(root));

        try {
            LNode node = walker.walk();
            Object rendered = node.render(context);
            return rendered == null ? "" : String.valueOf(rendered);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a string representation of the AST of the parsed input source.
     *
     * @return a string representation of the AST of the parsed input source.
     */
    public String toStringAST() {

        LimitedStringBuilder builder = new LimitedStringBuilder();

        walk(root, builder);

        return builder.toString();
    }

    /**
     * Walks a (sub) tree of the root of the input source and builds a string
     * representation of the structure of the AST.
     * <p/>
     * Note that line breaks and multiple white space characters are trimmed to
     * a single white space character.
     *
     * @param tree the (sub) tree.
     * @param builder the LimitedStringBuilder to fill.
     */
    @SuppressWarnings("unchecked")
    private void walk(CommonTree tree, LimitedStringBuilder builder) {

        List<CommonTree> firstStack = new ArrayList<CommonTree>();
        firstStack.add(tree);

        List<List<CommonTree>> childListStack = new ArrayList<List<CommonTree>>();
        childListStack.add(firstStack);

        while (!childListStack.isEmpty()) {

            List<CommonTree> childStack = childListStack.get(childListStack.size() - 1);

            if (childStack.isEmpty()) {
                childListStack.remove(childListStack.size() - 1);
            } else {
                tree = childStack.remove(0);

                String indent = "";

                for (int i = 0; i < childListStack.size() - 1; i++) {
                    indent += (childListStack.get(i).size() > 0) ? "|  " : "   ";
                }

                String tokenName = LiquidParser.tokenNames[tree.getType()];
                String tokenText = tree.getText().replaceAll("\\s+", " ").trim();

                builder.append(indent)
                        .append(childStack.isEmpty() ? "'- " : "|- ")
                        .append(tokenName)
                        .append(!tokenName.equals(tokenText) ? "='" + tokenText + "'" : "")
                        .append("\n");

                if (tree.getChildCount() > 0) {
                    childListStack.add(new ArrayList<CommonTree>((List<CommonTree>) tree.getChildren()));
                }
            }
        }
    }
}
