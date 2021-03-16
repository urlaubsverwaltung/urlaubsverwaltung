package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

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

    @Test
    void organizerIsNull() {
        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer(null);
        final Set<ConstraintViolation<CalendarProperties>> violations = validator.validate(calendarProperties);

        assertThat(violations.size()).isZero();
    }

    @Test
    void organizerIsAnEmptyString() {
        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("");
        final Set<ConstraintViolation<CalendarProperties>> violations = validator.validate(calendarProperties);

        assertThat(violations.size()).isOne();
    }

    @Test
    void organizerIsNotAnEmail() {
        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("NotAnEmail");
        final Set<ConstraintViolation<CalendarProperties>> violations = validator.validate(calendarProperties);

        assertThat(violations.size()).isOne();
    }
}
