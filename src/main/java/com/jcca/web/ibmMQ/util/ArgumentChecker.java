package com.jcca.web.ibmMQ.util;


import com.jcca.web.ibmMQ.common.ErrorConstants;
import com.jcca.web.ibmMQ.common.LocalizedIllegalArgumentException;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;

@ThreadSafe
public final class ArgumentChecker {
    public static void fail(String messageId, Object... bindings) {
        throw new LocalizedIllegalArgumentException(messageId, bindings);
    }

    public static void satisfy(boolean condition, String messageId, Object... bindings) {
        if (!condition) {

            throw new LocalizedIllegalArgumentException(messageId, bindings);

        }

    }

    public static void notNull(Object obj, String messageId, Object... bindings) {
        if (obj == null) {
            throw new LocalizedIllegalArgumentException(messageId, bindings);
        }
    }

    public static void notNullOrEmpty(Collection<?> collection, String messageId, Object... bindings) {
        if (collection == null || collection.isEmpty()) {
            throw new LocalizedIllegalArgumentException(messageId, bindings);

        }

    }

    public static void notNullOrEmpty(String str, String messageId, Object... bindings) {

        if (Strings.isNullOrEmpty(str))
            throw new LocalizedIllegalArgumentException(messageId, bindings);

    }


    public static void validateMetadataName(String name) {
        if (!Metadata.isValidMetadataName(name)) {
            throw new LocalizedIllegalArgumentException(ErrorConstants.Message.MSG_INVALID_METADATA_NAME, new Object[]{name});
        }
    }
}
