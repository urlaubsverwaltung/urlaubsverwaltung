package org.synyx.urlaubsverwaltung.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DecimalConverterTest {

    @Test
    void toDuration() {
        final BigDecimal tenHoursTwoMinutes = BigDecimal.valueOf(10.2);
        final Duration duration = DecimalConverter.toDuration(tenHoursTwoMinutes);
        assertThat(duration).isEqualTo(Duration.parse("PT10.2S"));
    }

    @Test
    void toDecimal() {
        final Duration tenHoursTwoMinutes = Duration.parse("PT10M2S");
        final BigDecimal bigDecimal = DecimalConverter.toFormattedDecimal(tenHoursTwoMinutes);
        assertThat(bigDecimal).isEqualByComparingTo(BigDecimal.valueOf(602));
    }

    @Test
    void toDecimalHours() {
        assertThat(DecimalConverter.toDecimalHours(Duration.ofMinutes(80))).isEqualByComparingTo("1.333");
        assertThat(DecimalConverter.toDecimalHours(Duration.ofMinutes(61))).isEqualByComparingTo("1.017");
        assertThat(DecimalConverter.toDecimalHours(Duration.ofMinutes(-90))).isEqualByComparingTo("-1.5");
        assertThat(DecimalConverter.toDecimalHours(Duration.ZERO)).isEqualByComparingTo("0");
    }
}
