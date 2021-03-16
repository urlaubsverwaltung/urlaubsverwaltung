package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;

class VacationTypeTest {

    @Test
    void ensureReturnsTrueIfVacationTypeIsOfGivenCategory() {

        final VacationType vacationType = new VacationType();
        vacationType.setCategory(OVERTIME);

        assertThat(vacationType.isOfCategory(OVERTIME)).isTrue();
    }

    @Test
    void ensureReturnsFalseIfVacationTypeIsNotOfGivenCategory() {

        final VacationType vacationType = new VacationType();
        vacationType.setCategory(HOLIDAY);

        assertThat(vacationType.isOfCategory(OVERTIME)).isFalse();
    }

    @Test
    void toStringTest() {
        final VacationType vacationType = new VacationType();
        vacationType.setCategory(HOLIDAY);
        vacationType.setMessageKey("messageKey");
        vacationType.setId(10);

        final String vacationTypeToString = vacationType.toString();
        assertThat(vacationTypeToString).isEqualTo("VacationType{category=HOLIDAY, messageKey='messageKey'}");
    }

    @Test
    void equals() {
        final VacationType vacationTypeOne = new VacationType();
        vacationTypeOne.setId(1);

        final VacationType vacationTypeOneOne = new VacationType();
        vacationTypeOneOne.setId(1);

        final VacationType vacationTypeTwo = new VacationType();
        vacationTypeTwo.setId(2);

        assertThat(vacationTypeOne)
            .isEqualTo(vacationTypeOne)
            .isEqualTo(vacationTypeOneOne)
            .isNotEqualTo(vacationTypeTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final VacationType vacationTypeOne = new VacationType();
        vacationTypeOne.setId(1);

        assertThat(vacationTypeOne.hashCode()).isEqualTo(32);
    }
}
