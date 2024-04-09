package com.jcca.web.ibmMQ.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */


public abstract class NLSResource {
    private static final Logger log = LoggerFactory.getLogger(NLSResource.class);

    private static final int MOD_EXPECTED = 9;

    private static final int MOD_MASK = 25;

    public static final IKeyResolver LOWERCASE_RESOLVER = new LowerCaseResolver();

    public static final IKeyResolver NORMAL_RESOLVER = new NormalResolver();

    public static final IKeyResolver NOOP_RESOLVER = new NoopResolver();

    public static final IKeyResolver DEFAULT_RESOLVER = LOWERCASE_RESOLVER;

    public static final String bind(String baseName, String messageId, Object... bindings) {
        return bind(baseName, Locale.getDefault(), messageId, bindings);
    }


    public static final String bind(String baseName, Locale locale, String messageId, Object... bindings) {
        ResourceBundle rb = null;
        try {
            rb = ResourceBundle.getBundle(baseName, (locale == null) ? Locale.getDefault() : locale);
        } catch (MissingResourceException e) {
            rb = ResourceBundle.getBundle(baseName, Locale.ENGLISH);

        }

        try {
            String message = rb.getString(messageId);
            return MessageFormat.format(message, bindings);
        } catch (MissingResourceException e) {
            log.warn("Missing message for '{}' in resource bundle '{}'!", messageId, baseName);
            return null;
        }
    }

    public static void initialize(Class<?> clazz) {
        initialize(clazz, DEFAULT_RESOLVER);
    }


    public static void initialize(Class<?> clazz, IKeyResolver resolver) {
        resolve(clazz, resolver);
    }


    private static void resolve(Class<?> clazz, IKeyResolver keyResolver) {

        boolean isAccessible = ((clazz.getModifiers() & 0x1) != 0);

        IKeyResolver resolver = (keyResolver == null) ? DEFAULT_RESOLVER : keyResolver;

        for (Field field : clazz.getDeclaredFields()) {

            if ((field.getModifiers() & 0x19) != 9 && field.getType() != String.class) {

                if (log.isDebugEnabled()) {
                    log.debug("Not valid field '{}', ignore it(expect public static final String field).", field.getName());

                }
            } else {
                String key = resolver.resolve(field);
                try {
                    if (!isAccessible) {
                        field.setAccessible(true);
                    }
                    field.set(null, key);
                } catch (Exception e) {
                    log.error("Exception when setting a field value.", e);
                }
            }
        }
    }


    public static interface IKeyResolver {
        String resolve(Field param1Field);

    }


    private static class LowerCaseResolver
            implements IKeyResolver {
        private LowerCaseResolver() {
        }


        public String resolve(Field field) {
            return field.getName().replace("_", ".").toLowerCase();
        }

    }


    private static class NormalResolver
            implements IKeyResolver {
        private NormalResolver() {
        }

        public String resolve(Field field) {
            return field.getName().replace("_", ".");
        }
    }


    private static class NoopResolver
            implements IKeyResolver {
        private NoopResolver() {
        }


        public String resolve(Field field) {
            return field.getName();
        }

    }
}
