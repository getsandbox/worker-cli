package org.jliquid.liqp;

public class LimitedStringBuilder {
    public static int limitInBytes = -1;
    StringBuilder builder = new StringBuilder();

    private void checkLengthWithinLimit(){
        if(limitInBytes > 0 && builder.length() >= limitInBytes){
            throw new IllegalArgumentException("Exceeded maximum size for template");
        }
    }

    public StringBuilder append(Object obj) {
        checkLengthWithinLimit();
        return builder.append(obj);
    }

    public StringBuilder append(String str) {
        checkLengthWithinLimit();
        return builder.append(str);
    }

    public StringBuilder append(StringBuffer sb) {
        checkLengthWithinLimit();
        return builder.append(sb);
    }

    public StringBuilder append(CharSequence s) {
        checkLengthWithinLimit();
        return builder.append(s);
    }

    public StringBuilder append(CharSequence s, int start, int end) {
        checkLengthWithinLimit();
        return builder.append(s, start, end);
    }

    public StringBuilder append(char[] str) {
        checkLengthWithinLimit();
        return builder.append(str);
    }

    public StringBuilder append(char[] str, int offset, int len) {
        checkLengthWithinLimit();
        return builder.append(str, offset, len);
    }

    public StringBuilder append(boolean b) {
        checkLengthWithinLimit();
        return builder.append(b);
    }

    public StringBuilder append(char c) {
        checkLengthWithinLimit();
        return builder.append(c);
    }

    public StringBuilder append(int i) {
        checkLengthWithinLimit();
        return builder.append(i);
    }

    public StringBuilder append(long lng) {
        checkLengthWithinLimit();
        return builder.append(lng);
    }

    public StringBuilder append(float f) {
        checkLengthWithinLimit();
        return builder.append(f);
    }

    public StringBuilder append(double d) {
        checkLengthWithinLimit();
        return builder.append(d);
    }

    public String toString() {
        return builder.toString();
    }
}
