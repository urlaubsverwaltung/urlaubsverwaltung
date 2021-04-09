package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarPropertiesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void organizerIsAnEmail() {
        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("email@example.org");
        final Set<ConstraintViolation<CalendarProperties>> violations = validator.validate(calendarProperties);

        assertThat(violations.size()).isZero();
    }

    static Stream<String> organizerStrings() {
        return Stream.of("", "NotAnEmail", null);
    }

    @ParameterizedTest
    @MethodSource("organizerStrings")
    void organizerIsWrong(String input) {
        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer(input);
        final Set<ConstraintViolation<CalendarProperties>> violations = validator.validate(calendarProperties);

        assertThat(violations.size()).isOne();
    }
}
