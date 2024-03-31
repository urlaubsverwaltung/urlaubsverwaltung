package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSettingsValidatorTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 32})
    void ensureExpiryDateDayOfMonthIsInvalid(int value) {

        final BindingResult errors = new MapBindingResult(new HashMap<>(), "");

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setExpiryDateDayOfMonth(value);

        AccountSettingsValidator.validateAccountSettings(accountSettings, errors);

        assertThat(errors.hasFieldErrors("accountSettings.expiryDateDayOfMonth")).isTrue();
    }

    static Stream<Arguments> validDayOfMonthValues() {
        return IntStream.rangeClosed(1, 31).mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("validDayOfMonthValues")
    void ensureExpiryDateDayOfMonthIsValid(int value) {

        final BindingResult errors = new MapBindingResult(new HashMap<>(), "");

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setExpiryDateDayOfMonth(value);

        AccountSettingsValidator.validateAccountSettings(accountSettings, errors);

        assertThat(errors.hasFieldErrors("accountSettings.expiryDateDayOfMonth")).isFalse();
    }
}
