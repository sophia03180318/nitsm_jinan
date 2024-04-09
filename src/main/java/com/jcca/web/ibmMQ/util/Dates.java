package com.jcca.web.ibmMQ.util;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.SoftReference;
import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@ThreadSafe
public final class Dates {
    private static final SimpleDateFormat dateFormat = new SafeSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat dateFormatNoTime = new SafeSimpleDateFormat("yyyy-MM-dd");

    private static final ThreadLocal<SoftReference<Calendar>> calendarRef = new ThreadLocal<SoftReference<Calendar>>() {
        /*     */
        public SoftReference<Calendar> get() {
            SoftReference<Calendar> softRef = super.get();
            if (softRef == null || softRef.get() == null) {
                softRef = new SoftReference<Calendar>(Calendar.getInstance());
                set(softRef);
            }
            return softRef;
        }
        /*     */
    };

    public static String format(Date date) {
        return dateFormat.format(date);
    }

    public static Date parse(String date) {
        if (Strings.isNullOrEmpty(date)) {
            return null;
        }
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            try {
                return dateFormatNoTime.parse(date);
            } catch (ParseException e1) {
                return null;
            }
        }
    }

    public static Date beforeNow(int amount, TimeUnit timeUnit) {
        return delta(new Date(), -amount, timeUnit);
    }

    public static Date afterNow(int amount, TimeUnit timeUnit) {
        return delta(new Date(), amount, timeUnit);
    }

    public static Date before(Date date, int amount, TimeUnit timeUnit) {
        return delta(date, -amount, timeUnit);
    }

    public static Date after(Date date, int amount, TimeUnit timeUnit) {
        return delta(date, amount, timeUnit);
    }

    public static Date delta(Date date, int amount, TimeUnit timeUnit) {
        Calendar calendar = ((SoftReference<Calendar>) calendarRef.get()).get();
        calendar.setTime(date);
        switch (timeUnit) {
            case DAYS:
                calendar.add(6, amount);
                return calendar.getTime();
            case HOURS:
                calendar.add(10, amount);
                return calendar.getTime();
            case MINUTES:
                calendar.add(12, amount);
                return calendar.getTime();
            case SECONDS:
                calendar.add(13, amount);
                return calendar.getTime();
        }
        throw new IllegalArgumentException("Unsupported TimeUnit:" + timeUnit);
    }

    public static class SafeSimpleDateFormat extends SimpleDateFormat {
        private static final long serialVersionUID = -8014148293861442146L;
        private final String dateFormat;
        private final ThreadLocal<SoftReference<SimpleDateFormat>> formatCache;

        public SafeSimpleDateFormat(String dateFormat) {

            this.formatCache = new ThreadLocal<SoftReference<SimpleDateFormat>>() {
                public SoftReference<SimpleDateFormat> get() {
                    SoftReference<SimpleDateFormat> softRef = super.get();
                    if (softRef == null || softRef.get() == null) {
                        softRef = new SoftReference<SimpleDateFormat>(new SimpleDateFormat(SafeSimpleDateFormat.this.dateFormat));
                        set(softRef);
                    }
                    return softRef;
                }
            };
            this.dateFormat = dateFormat;
        }

        private DateFormat getDateFormat() {
            return (this.formatCache.get()).get();
        }

        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
            return getDateFormat().format(date, toAppendTo, fieldPosition);
        }

        public Date parse(String source, ParsePosition pos) {
            return getDateFormat().parse(source, pos);
        }
    }
}