package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Locale;

import static java.time.ZoneOffset.UTC;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DateAndTimeFormatAwareTest {

    private DateFormatAware sut;

    @Mock
    private MessageSource messageSource;

    private final Locale locale = GERMAN;
    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new DateFormatAware(messageSource, clock);
        LocaleContextHolder.setLocale(locale);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.setLocale(Locale.getDefault());
    }

    @Test
    void ensureParseForIsoDateString() {
        assertThat(sut.parse("2020-10-30", locale)).hasValue(LocalDate.of(2020, 10, 30));
    }

    @Test
    void ensureParseForGermanDateString() {
        assertThat(sut.parse("30.10.2020", locale)).hasValue(LocalDate.of(2020, 10, 30));
    }

    @Test
    void ensureParseReturnsEmptyOptionalForUnknownDateFormat() {
        assertThat(sut.parse("30/10/2020", locale)).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void ensureParseReturnsEmptyOptionalForEmptyString(String givenDateString) {
        assertThat(sut.parse(givenDateString, locale)).isEmpty();
    }

    @Nested
    class FormatWord {

        @Test
        void ensureFormatWordReturnsTodayLowercase() {

            when(messageSource.getMessage("date.word.today", new Object[]{}, locale)).thenReturn("Heute");

            final String actual = sut.formatWord(LocalDate.now(), null);
            assertThat(actual).isEqualTo("heute");
        }

        @Test
        void ensureFormatWordReturnsTomorrowLowercase() {

            when(messageSource.getMessage("date.word.tomorrow", new Object[]{}, locale)).thenReturn("Morgen");

            final String actual = sut.formatWord(LocalDate.now().plusDays(1), null);
            assertThat(actual).isEqualTo("morgen");
        }

        @ParameterizedTest
        @CsvSource({
            "de, FULL,   'Montag, 22. Juli 2024'",
            "de, LONG,   '22. Juli 2024'",
            "de, MEDIUM, '22.07.2024'",
            "de, SHORT,  '22.07.24'",
            "en, FULL,   'Monday, July 22, 2024'",
            "en, LONG,   'July 22, 2024'",
            "en, MEDIUM, 'Jul 22, 2024'",
            "en, SHORT,  '7/22/24'",
        })
        void ensureFormatWordReturnsDate(String languageTag, String formatStyle, String expected) {

            final Clock fixedClock = Clock.fixed(Instant.parse("2024-07-20T00:00:00Z"), UTC);

            LocaleContextHolder.setLocale(Locale.forLanguageTag(languageTag));
            sut = new DateFormatAware(messageSource, fixedClock);

            final String actual = sut.formatWord(LocalDate.now(fixedClock).plusDays(2), FormatStyle.valueOf(formatStyle));
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void ensureFormatWordReturnsTodayWithTransformation() {

            when(messageSource.getMessage("date.word.today", new Object[]{}, locale)).thenReturn("Heute");

            final String actual = sut.formatWord(LocalDate.now(), null, String::toUpperCase);
            assertThat(actual).isEqualTo("HEUTE");
        }

        @Test
        void ensureFormatWordReturnsTomorrowWithTransformation() {

            when(messageSource.getMessage("date.word.tomorrow", new Object[]{}, locale)).thenReturn("Morgen");

            final String actual = sut.formatWord(LocalDate.now().plusDays(1), null, String::toUpperCase);
            assertThat(actual).isEqualTo("MORGEN");
        }

        @ParameterizedTest
        @CsvSource({
            "de, FULL,   'Montag, 22. Juli 2024'",
            "de, LONG,   '22. Juli 2024'",
            "de, MEDIUM, '22.07.2024'",
            "de, SHORT,  '22.07.24'",
            "en, FULL,   'Monday, July 22, 2024'",
            "en, LONG,   'July 22, 2024'",
            "en, MEDIUM, 'Jul 22, 2024'",
            "en, SHORT,  '7/22/24'",
        })
        void ensureFormatWordReturnsDateWithoutTransformation(String languageTag, String formatStyle, String expected) {

            final Clock fixedClock = Clock.fixed(Instant.parse("2024-07-20T00:00:00Z"), UTC);

            LocaleContextHolder.setLocale(Locale.forLanguageTag(languageTag));
            sut = new DateFormatAware(messageSource, fixedClock);

            final String actual = sut.formatWord(LocalDate.now(fixedClock).plusDays(2), FormatStyle.valueOf(formatStyle), String::toUpperCase);
            assertThat(actual).isEqualTo(expected);
        }
    }
}
