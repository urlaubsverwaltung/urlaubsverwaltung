package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.time.Month;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;
import static org.assertj.core.api.Assertions.assertThat;

class AccountSettingsValidatorTest {

    static Stream<Arguments> invalidDayOfMonthValues() {

        BiFunction<Month, Stream<Integer>, Stream<Arguments>> dayOfMonths =
            (month, days) -> days.map(day -> Arguments.of(month, day));

        return Stream.of(Month.values()).flatMap(month -> switch (month) {
            case JANUARY -> dayOfMonths.apply(JANUARY, Stream.of(-1, 0, 32));
            case FEBRUARY -> dayOfMonths.apply(FEBRUARY, Stream.of(-1, 0, 30));
            case MARCH -> dayOfMonths.apply(MARCH, Stream.of(-1, 0, 32));
            case APRIL -> dayOfMonths.apply(APRIL, Stream.of(-1, 0, 31));
            case MAY -> dayOfMonths.apply(MAY, Stream.of(-1, 0, 32));
            case JUNE -> dayOfMonths.apply(JUNE, Stream.of(-1, 0, 31));
            case JULY -> dayOfMonths.apply(JULY, Stream.of(-1, 0, 32));
            case AUGUST -> dayOfMonths.apply(AUGUST, Stream.of(-1, 0, 32));
            case SEPTEMBER -> dayOfMonths.apply(SEPTEMBER, Stream.of(-1, 0, 31));
            case OCTOBER -> dayOfMonths.apply(OCTOBER, Stream.of(-1, 0, 32));
            case NOVEMBER -> dayOfMonths.apply(NOVEMBER, Stream.of(-1, 0, 31));
            case DECEMBER -> dayOfMonths.apply(DECEMBER, Stream.of(-1, 0, 32));
        });
    }

    @ParameterizedTest
    @MethodSource("invalidDayOfMonthValues")
    void ensureExpiryDateDayOfMonthIsInvalid(Month month, int dayOfMonth) {

        final BindingResult errors = new MapBindingResult(new HashMap<>(), "");

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setExpiryDateMonth(month);
        accountSettings.setExpiryDateDayOfMonth(dayOfMonth);

        AccountSettingsValidator.validateAccountSettings(accountSettings, errors);

        assertThat(errors.hasFieldErrors("accountSettings.expiryDateDayOfMonth")).isTrue();
    }

    static Stream<Arguments> validDayOfMonthValues() {

        BiFunction<Month, IntStream, Stream<Arguments>> dayOfMonths =
            (month, days) -> days.mapToObj(day -> Arguments.of(month, day));

        return Stream.of(Month.values()).flatMap(month -> switch (month) {
            case JANUARY -> dayOfMonths.apply(JANUARY, IntStream.rangeClosed(1, 31));
            case FEBRUARY -> dayOfMonths.apply(FEBRUARY, IntStream.rangeClosed(1, 28));
            case MARCH -> dayOfMonths.apply(MARCH, IntStream.rangeClosed(1, 31));
            case APRIL -> dayOfMonths.apply(APRIL, IntStream.rangeClosed(1, 30));
            case MAY -> dayOfMonths.apply(MAY, IntStream.rangeClosed(1, 31));
            case JUNE -> dayOfMonths.apply(JUNE, IntStream.rangeClosed(1, 30));
            case JULY -> dayOfMonths.apply(JULY, IntStream.rangeClosed(1, 31));
            case AUGUST -> dayOfMonths.apply(AUGUST, IntStream.rangeClosed(1, 31));
            case SEPTEMBER -> dayOfMonths.apply(SEPTEMBER, IntStream.rangeClosed(1, 30));
            case OCTOBER -> dayOfMonths.apply(OCTOBER, IntStream.rangeClosed(1, 31));
            case NOVEMBER -> dayOfMonths.apply(NOVEMBER, IntStream.rangeClosed(1, 30));
            case DECEMBER -> dayOfMonths.apply(DECEMBER, IntStream.rangeClosed(1, 31));
        });
    }

    @ParameterizedTest
    @MethodSource("validDayOfMonthValues")
    void ensureExpiryDateDayOfMonthIsValid(Month month, int dayOfMonth) {

        final BindingResult errors = new MapBindingResult(new HashMap<>(), "");

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setExpiryDateMonth(month);
        accountSettings.setExpiryDateDayOfMonth(dayOfMonth);

        AccountSettingsValidator.validateAccountSettings(accountSettings, errors);

        assertThat(errors.hasFieldErrors("accountSettings.expiryDateDayOfMonth")).isFalse();
    }
}
