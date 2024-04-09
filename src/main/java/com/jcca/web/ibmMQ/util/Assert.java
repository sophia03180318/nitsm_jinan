/*     */
package com.jcca.web.ibmMQ.util;
/*     */
/*     */

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;

/*     */

@ThreadSafe
public final class Assert {
    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNullOrEmpty(String str) {
        notNullOrEmpty(str, "[Assertion failed] - this argument cannot be null or empty(only spaces is also considered as empty)!");
    }

    public static void notNullOrEmpty(String str, String message) {
        if (str == null || str.trim().equals("")) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNullOrEmpty(Object[] array) {
        notNullOrEmpty(array, "[Assertion failed] - this argument cannot be null or empty(no elements in this array)!");
    }

    public static void notNullOrEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNullOrEmpty(Collection collection) {
        notNullOrEmpty(collection, "[Assertion failed] - this collection must not be null or empty: it must not be null and must contain at least 1 element");
    }

    public static void notNullOrEmpty(Collection collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static void state(boolean expression) {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }

    public static void satisfy(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void satisfy(boolean expression) {
        satisfy(expression, "[Assertion failed] - this state invariant must be true");
    }
}
