package org.synyx.urlaubsverwaltung.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.Duration;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DurationFormatterTest {

    public static final Locale GERMAN = Locale.GERMAN;

    @Mock
    private MessageSource messageSource;

    @Test
    void ensuresDurationStringWithHoursAndMinutes() {

        when(messageSource.getMessage("hours.abbr", new Object[]{}, GERMAN)).thenReturn("Std.");
        when(messageSource.getMessage("minutes.abbr", new Object[]{}, GERMAN)).thenReturn("Min.");

        final Duration duration = Duration.ofMinutes(23 * 60 + 32);
        final String durationString = DurationFormatter.toDurationString(duration, messageSource, GERMAN);
        assertThat(durationString).isEqualTo("23 Std. 32 Min.");
    }

    @Test
    void ensuresDurationStringWithMoreThanOneDayWithMinutes() {

        when(messageSource.getMessage("hours.abbr", new Object[]{}, GERMAN)).thenReturn("Std.");
        when(messageSource.getMessage("minutes.abbr", new Object[]{}, GERMAN)).thenReturn("Min.");

        final Duration duration = Duration.ofDays(2).plus(Duration.ofHours(2)).plus(Duration.ofMinutes(61));
        final String durationString = DurationFormatter.toDurationString(duration, messageSource, GERMAN);
        assertThat(durationString).isEqualTo("51 Std. 1 Min.");
    }

    @Test
    void ensuresDurationStringWithOnlyMinutes() {

        when(messageSource.getMessage("minutes.abbr", new Object[]{}, GERMAN)).thenReturn("Min.");

        final Duration duration = Duration.ofMinutes(1);
        final String durationString = DurationFormatter.toDurationString(duration, messageSource, GERMAN);
        assertThat(durationString).isEqualTo("1 Min.");
    }

    @Test
    void ensuresDurationStringIsNegative() {

        when(messageSource.getMessage("hours.abbr", new Object[]{}, GERMAN)).thenReturn("Std.");

        final Duration duration = Duration.ofDays(-1);
        final String durationString = DurationFormatter.toDurationString(duration, messageSource, GERMAN);
        assertThat(durationString).isEqualTo("-24 Std.");
    }

    @Test
    void ensuresEmptyStringOnNull() {
        final String durationString = DurationFormatter.toDurationString(null, messageSource, GERMAN);
        assertThat(durationString).isEmpty();
    }

    @Test
    void ensuresWithEmptyHoursAndMinutes() {

        when(messageSource.getMessage("overtime.person.zero", new Object[]{}, GERMAN)).thenReturn("keine");

        final String durationString = DurationFormatter.toDurationString(Duration.ofSeconds(0), messageSource, GERMAN);
        assertThat(durationString).isEqualTo("keine");
    }
}
