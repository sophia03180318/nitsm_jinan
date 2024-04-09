package com.jcca.web.ibmMQ.common;


import com.jcca.web.ibmMQ.util.ArgumentChecker;
import com.jcca.web.ibmMQ.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@ThreadSafe
public final class Time
        implements Comparable<Time> {
    private static final Map<String, TimeUnit> timeUnitMapping = new HashMap<String, TimeUnit>();
    private static final Pattern patternSMHD = Pattern.compile("^[1-9]\\d*(s|m|h|d)$", 2);
    private static final Pattern patternMHD = Pattern.compile("^[1-9]\\d*(m|h|d)$", 2);

    public static final int SMHD = 1;


    public static final int MHD = 2;

    private final int value;

    private final TimeUnit unit;

    static {

        createMapping(new TimeUnit[]{TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS});

    }


    private Time(int value, TimeUnit unit) {
        if (unit == TimeUnit.MILLISECONDS || unit == TimeUnit.MICROSECONDS || unit == TimeUnit.NANOSECONDS) {

            throw new IllegalArgumentException(String.format("%s is not supported", new Object[]{unit}));
        }
        this.value = value;
        this.unit = unit;
    }


    public int getValue() {
        return this.value;
    }


    public TimeUnit getUnit() {
        return this.unit;
    }


    public static void validateMHD(String timestr) {
        validate(timestr, 2, null);
    }


    public static void validateMHD(String timestr, String message) {
        validate(timestr, 2, message);
    }


    public static void validateSMHD(String timestr) {
        validate(timestr, 1, null);
    }


    public static void validateSMHD(String timestr, String message) {
        validate(timestr, 1, message);
    }


    public static void validate(String timestr, int supportType) {
        validate0(timestr, supportType, null);
    }

    public static void validate(String timestr, int supportType, String message) {
        validate0(timestr, supportType, message);
    }

    private static void validate0(String timestr, int supportType, String message) {

        ArgumentChecker.notNullOrEmpty(timestr, ErrorConstants.Message.MSG_INVALID_TIME_FORMAT_1, new Object[0]);

        Pattern p = getTimePattern(supportType);

        Assert.notNull(p, String.format("Unsupported time pattern '%d' found!", new Object[]{Integer.valueOf(supportType)}));

        if (!p.matcher(normalize(timestr)).matches()) {

            if (supportType == 1) {

                ArgumentChecker.fail(ErrorConstants.Message.MSG_INVALID_TIME_FORMAT_SMHD, new Object[]{(message == null) ? "" : (message + ";"), timestr});

            }

            ArgumentChecker.fail(ErrorConstants.Message.MSG_INVALID_TIME_FORMAT_MHD, new Object[]{(message == null) ? "" : (message + ";"), timestr});

        }

    }


    public static Time parseSMHD(String timestr) {
        return parse(timestr, 1, null);
    }


    public static Time parseSMHD(String timestr, String message) {
        return parse(timestr, 1, message);
    }


    public static Time parseMHD(String timestr) {
        return parse(timestr, 2, null);
    }


    public static Time parseMHD(String timestr, String message) {
        return parse(timestr, 2, message);
    }


    public static Time parse(String timestr, int supportType) {
        return parse(timestr, supportType, null);
    }


    public static Time parse(String timestr, int supportType, String message) {

        validate0(timestr, supportType, message);

        String normalizedStr = normalize(timestr);

        Matcher m = null;

        Pattern p = getTimePattern(supportType);

        if ((m = p.matcher(normalizedStr)).matches()) {

            String valuestr = normalizedStr.substring(0, m.end(1) - 1);

            TimeUnit timeUnit = timeUnitMapping.get(m.group(1));

            if (timeUnit == null) {

                throw new IllegalArgumentException("Invalid time format; time unit:" + m.group(1) + " is not supported!");

            }

            int v = 0;

            try {
                v = Integer.parseInt(valuestr);
            } catch (Exception e) {
                throw new IllegalArgumentException("Time cannot be parsed, the value should be numeric!", e);
            }
            return new Time(v, timeUnit);
        }

        throw new IllegalArgumentException("Invalid time format, failed to parse!");
    }

    private static String normalize(String s) {
        return s.replaceAll("\\s+", "");
    }

    private static Pattern getTimePattern(int type) {
        switch (type) {
            case 2:
                return patternMHD;
            case 1:
                return patternSMHD;
        }
        throw new IllegalArgumentException("Unsupported time type!");
    }

    private static void createMapping(TimeUnit... supportedUnits) {
        for (TimeUnit unit : supportedUnits) {
            timeUnitMapping.put(unit.name().toLowerCase().substring(0, 1), unit);

        }
    }

    public long convertTo(TimeUnit targetUnit) {
        return targetUnit.convert(this.value, this.unit);
    }

    public boolean is(Operation op, Time another) {
        Assert.notNull(op);
        Assert.notNull(another, "time for comparsion cannot be null");
        boolean result = false;
        switch (op) {
            case LT:
                result = (compareTo(another) < 0);
                break;
            case LE:
                result = (compareTo(another) <= 0);
                break;
            case GT:
                result = (compareTo(another) > 0);
                break;
            case GE:
                result = (compareTo(another) >= 0);
                break;
            case EQ:
                result = (compareTo(another) == 0);
                break;
            case NE:
                result = (compareTo(another) != 0);
                break;
        }
        return result;
    }

    public void ensureSatisfy(Operation op, Time another, String errorMessage) {
        if (!is(op, another)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public enum Operation {
        LT,
        LE,
        GT,
        GE,
        EQ,
        NE;
    }

    public int compareTo(Time that) {
        if (this == that) {
            return 0;
        }

        long thisVal = TimeUnit.SECONDS.convert(this.value, this.unit);
        long thatVal = TimeUnit.SECONDS.convert(that.getValue(), that.getUnit());
        return (thisVal < thatVal) ? -1 : ((thisVal == thatVal) ? 0 : 1);
    }

    public String toString() {
        return this.value + " " + this.unit.toString().toLowerCase();
    }
}

