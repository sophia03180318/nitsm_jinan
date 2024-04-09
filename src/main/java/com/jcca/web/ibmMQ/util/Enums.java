package com.jcca.web.ibmMQ.util;


import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;

/*    */
/*    */


@ThreadSafe
public final class Enums {

    public static <E extends Enum<E>> E valueOf(String enumstr, Class<E> enumType) {
        for (Enum enum_ : (Enum[]) enumType.getEnumConstants()) {
            if (enum_.name().equalsIgnoreCase(enumstr)) {
                return (E) enum_;
            }
        }
        return null;
    }

    public static <E extends Enum<E>> List<String> toList(Class<E> enumType) {
        ArrayList<String> enumstrList = new ArrayList<String>();
        for (Enum enum_ : (Enum[]) enumType.getEnumConstants()) {
            enumstrList.add(enum_.name());
        }
        enumstrList.trimToSize();
        return enumstrList;
    }
}

