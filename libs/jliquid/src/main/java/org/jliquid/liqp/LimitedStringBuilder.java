package org.jliquid.liqp;

public class LimitedStringBuilder {
    public static int limitInBytes = -1;
    private StringBuilder builder;

    private StringBuilder getBuilder() {
        if (builder == null) {
            builder = new StringBuilder();
        }
        return builder;
    }

    private void checkLengthWithinLimit(){
        if(limitInBytes > 0 && getBuilder().length() >= limitInBytes){
            throw new IllegalArgumentException("Exceeded maximum size for template");
        }
    }

    public StringBuilder append(Object obj) {
        checkLengthWithinLimit();
        return getBuilder().append(obj);
    }

    public StringBuilder append(String str) {
        checkLengthWithinLimit();
        return getBuilder().append(str);
    }

    public StringBuilder append(StringBuffer sb) {
        checkLengthWithinLimit();
        return getBuilder().append(sb);
    }

    public StringBuilder append(CharSequence s) {
        checkLengthWithinLimit();
        return getBuilder().append(s);
    }

    public StringBuilder append(CharSequence s, int start, int end) {
        checkLengthWithinLimit();
        return getBuilder().append(s, start, end);
    }

    public StringBuilder append(char[] str) {
        checkLengthWithinLimit();
        return getBuilder().append(str);
    }

    public StringBuilder append(char[] str, int offset, int len) {
        checkLengthWithinLimit();
        return getBuilder().append(str, offset, len);
    }

    public StringBuilder append(boolean b) {
        checkLengthWithinLimit();
        return getBuilder().append(b);
    }

    public StringBuilder append(char c) {
        checkLengthWithinLimit();
        return getBuilder().append(c);
    }

    public StringBuilder append(int i) {
        checkLengthWithinLimit();
        return getBuilder().append(i);
    }

    public StringBuilder append(long lng) {
        checkLengthWithinLimit();
        return getBuilder().append(lng);
    }

    public StringBuilder append(float f) {
        checkLengthWithinLimit();
        return getBuilder().append(f);
    }

    public StringBuilder append(double d) {
        checkLengthWithinLimit();
        return getBuilder().append(d);
    }

    public String toString() {
        return builder == null ? "" : getBuilder().toString();
    }
}
