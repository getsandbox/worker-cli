# Liqp - jliquid-liqp [![Build Status/master] (https://travis-ci.org/matthiasdeck/jliquid-liqp.png?branch=master)](https://travis-ci.org/matthiasdeck/jliquid-liqp)

A Java implementation of the [Liquid templating engine](http://wiki.shopify.com/Liquid) backed 
up by an ANTLR grammar. 

This project is a mavenized fork of [Liqp](https://github.com/bkiers/Liqp).
    
This library can be used in two different ways:

1. to construct an AST (abstract syntax tree) of some Liquid input
2. to render Liquid input source (either files, or input strings)

## 1. Creating an AST

To create an AST from input source, do the following:

```java
String input =
        "<ul id=\"products\">                                       \n" +
        "  {% for product in products %}                            \n" +
        "    <li>                                                   \n" +
        "      <h2>{{ product.name }}</h2>                          \n" +
        "      Only {{ product.price | price }}                     \n" +
        "                                                           \n" +
        "      {{ product.description | prettyprint | paragraph }}  \n" +
        "    </li>                                                  \n" +
        "  {% endfor %}                                             \n" +
        "</ul>                                                      \n";
Template template = Template.parse(input);

CommonTree root = template.getAST();
```

As you can see, the `getAST()` method returns an instance of a 
[`CommonTree`](http://www.antlr.org/api/Java/org/antlr/runtime/tree/CommonTree.html) denoting the root 
node of the input source. To see how the AST is built, you can use `Template#toStringAST()` to print 
an ASCII representation of the tree:

```java
System.out.println(template.toStringAST());
/*
    '- BLOCK
       |- PLAIN='<ul id="products">'
       |- FOR_ARRAY
       |  |- Id='product'
       |  |- LOOKUP
       |  |  '- Id='products'
       |  |- BLOCK
       |  |  |- PLAIN='<li> <h2>'
       |  |  |- OUTPUT
       |  |  |  |- LOOKUP
       |  |  |  |  |- Id='product'
       |  |  |  |  '- Id='name'
       |  |  |  '- FILTERS
       |  |  |- PLAIN='</h2> Only'
       |  |  |- OUTPUT
       |  |  |  |- LOOKUP
       |  |  |  |  |- Id='product'
       |  |  |  |  '- Id='price'
       |  |  |  '- FILTERS
       |  |  |     '- FILTER
       |  |  |        |- Id='price'
       |  |  |        '- PARAMS
       |  |  |- PLAIN=''
       |  |  |- OUTPUT
       |  |  |  |- LOOKUP
       |  |  |  |  |- Id='product'
       |  |  |  |  '- Id='description'
       |  |  |  '- FILTERS
       |  |  |     |- FILTER
       |  |  |     |  |- Id='prettyprint'
       |  |  |     |  '- PARAMS
       |  |  |     '- FILTER
       |  |  |        |- Id='paragraph'
       |  |  |        '- PARAMS
       |  |  '- PLAIN='</li>'
       |  '- ATTRIBUTES
       '- PLAIN='</ul>'
*/
```
Checkout the [ANTLR grammar](https://github.com/bkiers/Liqp/blob/master/src/grammar/Liquid.g) 
to see what the AST looks like for each of the parser rules.

## 2. Render Liquid

If you're not familiar with Liquid, have a look at their website: [http://liquidmarkup.org](liquidmarkup.org).

In Ruby, you'd render a template like this:

```ruby
@template = Liquid::Template.parse("hi {{name}}")  # Parses and compiles the template
@template.render( 'name' => 'tobi' )               # Renders the output => "hi tobi"
```

With Liqp, the equivalent looks like this:

```java
Template template = Template.parse("hi {{name}}");
String rendered = template.render("name", "tobi");
System.out.println(rendered);
/*
    hi tobi
*/
```
The context provided as a parameter to `render(...)` can be:

* a [varargs](http://docs.oracle.com/javase/1.5.0/docs/guide/language/varargs.html) where 
  the 0<sup>th</sup>, 2<sup>nd</sup>, 4<sup>th</sup>, ... indexes must be `String` literals
  denoting the keys. The values can be any `Object`.
* a `Map<String, Object>`
* or a JSON string

The following examples are equivalent to the previous Liqp example:

#### Map example

```java
Template template = Template.parse("hi {{name}}");
Map<String, Object> map = new HashMap<String, Object>();
map.put("name", "tobi");
String rendered = template.render(map);
System.out.println(rendered);
/*
    hi tobi
*/
```

#### JSON example

```java
Template template = Template.parse("hi {{name}}");
String rendered = template.render("{\"name\" : \"tobi\"}");
System.out.println(rendered);
/*
    hi tobi
*/
```

### 2.1 Custom filters

Let's say you want to create a custom filters, called `b`, that changes a string like 
`*text*` to `<strong>text</strong>`.

You can do that as follows:

```java
// first register your custom filter
Filter.registerFilter(new Filter("b"){
    @Override
    public Object apply(Object value, Object... params) {
        // create a string from the  value
        String text = super.asString(value);

        // replace and return *...* with <strong>...</strong>
        return text.replaceAll("\\*(\\w(.*?\\w)?)\\*", "<strong>$1</strong>");
    }
});

// use your filter
Template template = Template.parse("{{ wiki | b }}");
String rendered = template.render("{\"wiki\" : \"Some *bold* text *in here*.\"}");
System.out.println(rendered);
/*
    Some <strong>bold</strong> text <strong>in here</strong>.
*/
```
And to use an optional parameter in your filter, do something like this:

```java
// first register your custom filter
Filter.registerFilter(new Filter("repeat"){
    @Override
    public Object apply(Object value, Object... params) {

        // get the text of the value
        String text = super.asString(value);
        
        // check if an optional parameter is provided
        int times = params.length == 0 ? 1 : super.asNumber(params[0]).intValue();

        StringBuilder builder = new StringBuilder();

        while(times-- > 0) {
            builder.append(text);
        }

        return builder.toString();
    }
});

// use your filter
Template template = Template.parse("{{ 'a' | repeat }}\n{{ 'b' | repeat:5 }}");
String rendered = template.render();
System.out.println(rendered);
/*
    a
    bbbbb
*/
```
You can use an array (or list) as well, and can also return a numerical value:

```java
Filter.registerFilter(new Filter("sum"){
    @Override
    public Object apply(Object value, Object... params) {

        Object[] numbers = super.asArray(value);

        double sum = 0;

        for(Object obj : numbers) {
            sum += super.asNumber(obj).doubleValue();
        }

        return sum;
    }
});

Template template = Template.parse("{{ numbers | sum }}");
String rendered = template.render("{\"numbers\" : [1, 2, 3, 4, 5]}");
System.out.println(rendered);
/*
    15.0
*/
```

### 2.2 Custom tags

Let's say you would like to create a tag that makes it easy to loop for a fixed amount of times,
executing a block of Liquid code.

Here's a way to create, and use, such a custom `loop` tag:

```java
Tag.registerTag(new Tag("loop"){
    @Override
    public Object render(Map<String, Object> context, LNode... nodes) {

        int n = super.asNumber(nodes[0].render(context)).intValue();
        LNode block = nodes[1];

        StringBuilder builder = new StringBuilder();

        while(n-- > 0) {
            builder.append(super.asString(block.render(context)));
        }

        return builder.toString();
    }
});

Template template = Template.parse("{% loop 5 %}looping!\n{% endloop %}");
String rendered = template.render();
System.out.println(rendered);
/*
    looping!
    looping!
    looping!
    looping!
    looping!
*/
```
