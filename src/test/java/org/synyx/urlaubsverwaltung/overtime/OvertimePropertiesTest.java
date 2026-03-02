package org.synyx.urlaubsverwaltung.overtime;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimePropertiesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.getValidator();
        }
    }

    @Test
    void helpUrlDefault() {
        final OvertimeProperties overtimeProperties = new OvertimeProperties();
        assertThat(overtimeProperties.getZeiterfassungLockSettingsUrl()).isEqualTo("https://urlaubsverwaltung.cloud/hilfe/zeiterfassung/zeiteintraege/#koennen-zeiteintraege-festgeschrieben-werden");
    }

    @Test
    void helpUrlIsGiven() {
        final OvertimeProperties overtimeProperties = new OvertimeProperties();
        overtimeProperties.setZeiterfassungLockSettingsUrl("https://urlaubsverwaltung.cloud/hilfe/zeiterfassung/zeiteintraege/#koennen-zeiteintraege-festgeschrieben-werden");
        final Set<ConstraintViolation<OvertimeProperties>> violations = validator.validate(overtimeProperties);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "NotAnUrl"})
    @NullSource
    void helpUrlIsWrong(String input) {
        final OvertimeProperties menuProperties = new OvertimeProperties();
        menuProperties.setZeiterfassungLockSettingsUrl(input);
        final Set<ConstraintViolation<OvertimeProperties>> violations = validator.validate(menuProperties);

        assertThat(violations.size()).isOne();
    }

}
