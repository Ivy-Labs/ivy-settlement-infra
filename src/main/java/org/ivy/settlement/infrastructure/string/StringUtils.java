package org.ivy.settlement.infrastructure.string;

import org.slf4j.helpers.MessageFormatter;

/**
 * String utility functions.
 */
public class StringUtils {

    private StringUtils() {}





    public static String zeros(int n) {
        return repeat('0', n);
    }

    public static String repeat(char value, int n) {
        return new String(new char[n]).replace("\0", String.valueOf(value));
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen = length(cs);
        if (strLen == 0) {
            return true;
        } else {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }


    public static String format(String template, Object...params) {
        return MessageFormatter.arrayFormat(template, params).getMessage();
    }

    public static void main(String[] args) {
        String template = "sd is {} and i am{}  --{}, {}";
        System.out.println(MessageFormatter.arrayFormat(template, new Object[]{"params", 1, 2.3, true}).getMessage());
    }
}
