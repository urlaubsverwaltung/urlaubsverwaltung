package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class VacationTypeTest {

    @Test
    void ensureThrowsIfCheckingCategoryWithNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new VacationType().isOfCategory(null));
    }


    @Test
    void ensureReturnsTrueIfVacationTypeIsOfGivenCategory() {

        VacationType vacationType = new VacationType();
        vacationType.setCategory(VacationCategory.OVERTIME);

        assertThat(vacationType.isOfCategory(VacationCategory.OVERTIME)).isTrue();
    }


    @Test
    void ensureReturnsFalseIfVacationTypeIsNotOfGivenCategory() {

        VacationType vacationType = new VacationType();
        vacationType.setCategory(VacationCategory.HOLIDAY);

        assertThat(vacationType.isOfCategory(VacationCategory.OVERTIME)).isFalse();
    }

    @Test
    void toStringTest() {
        VacationType vacationType = new VacationType();
        vacationType.setCategory(VacationCategory.HOLIDAY);
        vacationType.setMessageKey("messageKey");
        vacationType.setId(10);

        final String vacationTypeToString = vacationType.toString();
        assertThat(vacationTypeToString).isEqualTo("VacationType{category=HOLIDAY, messageKey='messageKey'}");
    }
}
