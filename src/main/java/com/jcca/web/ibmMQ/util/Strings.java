package com.jcca.web.ibmMQ.util;


import javax.annotation.concurrent.ThreadSafe;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*     */
/*     */
/*     */
/*     */
/*     */


@ThreadSafe
public final class Strings {
    private static final String NULL = "null";
    private static final Pattern VAR_PATTERN_UNIX = Pattern.compile("\\$(?:(\\w+)|\\{(\\w+)\\})");
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    public static String substituteUnix(String template, Map<String, String> variables) {
        Matcher matcher = VAR_PATTERN_UNIX.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            if (variables.containsKey(varName)) {
                String replacement = variables.get(varName);
                matcher.appendReplacement(buffer, (replacement != null) ? Matcher.quoteReplacement(replacement) : "null");
            }
            varName = matcher.group(2);
            if (variables.containsKey(varName)) {
                String replacement = variables.get(varName);
                matcher.appendReplacement(buffer, (replacement != null) ? Matcher.quoteReplacement(replacement) : "null");
            }
        }
        return matcher.appendTail(buffer).toString();
    }

    public static String substitute(String template, Map<String, String> variables) {
        Matcher matcher = VAR_PATTERN.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            if (variables.containsKey(matcher.group(1))) {
                String replacement = variables.get(matcher.group(1));
                matcher.appendReplacement(buffer, (replacement != null) ? Matcher.quoteReplacement(replacement) : "null");
            }
        }
        return matcher.appendTail(buffer).toString();
    }


    public static String join(Iterable<?> iterable, String delimiter) {
        StringBuffer buffer = new StringBuffer();

        Iterator<?> iter = iterable.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

    public static boolean isNullOrEmpty(String s) {
        return (s == null || s.trim().equals(""));
    }

    public static String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        CharBuffer buf = CharBuffer.wrap(str.toCharArray());
        return buf.put(0, Character.toTitleCase(buf.get(0))).toString();
    }

    public static String uncapitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        CharBuffer buf = CharBuffer.wrap(str.toCharArray());
        return buf.put(0, Character.toLowerCase(buf.get(0))).toString();
    }

    public static boolean trimmedEquals(String str1, String str2) {
        return (str1 == str2 || (str1 != null && str1.trim().equals((str2 != null) ? str2.trim() : null)));
    }
}
