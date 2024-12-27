package org.synyx.urlaubsverwaltung.util;

import java.math.BigDecimal;
import java.time.Duration;

public class DecimalConverter {

    private static final char[] ZEROES = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0'};
    private static final BigDecimal ONE_BILLION = new BigDecimal(1000000000L);

    private DecimalConverter() {
        // ok
    }

    /**
     * Converts a {@link BigDecimal} with seconds into a {@link Duration}
     *
     * @param bigDecimal to convert into duration
     * @return the duration with the bigDecimal of the big decimal
     */
    public static Duration toDuration(BigDecimal bigDecimal) {
        long seconds = bigDecimal.longValue();
        int nanoseconds = extractNanosecondDecimal(bigDecimal, seconds);
        return Duration.ofSeconds(seconds, nanoseconds);
    }

    /**
     * Converts a {@link Duration} into {@link BigDecimal} seconds
     *
     * @param duration to convert into big decimal
     * @return the big decimal in seconds with the value of the duration
     */
    public static BigDecimal toFormattedDecimal(Duration duration) {
        return new BigDecimal(toFormattedDecimal(duration.getSeconds(), duration.getNano()));
    }

    private static String toFormattedDecimal(long seconds, int nanoseconds) {
        final StringBuilder string = new StringBuilder(Integer.toString(nanoseconds));
        if (string.length() < 9) {
            string.insert(0, ZEROES, 0, 9 - string.length());
        }
        return seconds + "." + string;
    }

    private static int extractNanosecondDecimal(BigDecimal value, long integer) {
        return value.subtract(new BigDecimal(integer)).multiply(ONE_BILLION).intValue();
    }
}
