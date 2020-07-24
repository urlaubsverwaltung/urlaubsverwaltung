package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.Assert;
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

        Assert.assertTrue("Categories should match", vacationType.isOfCategory(VacationCategory.OVERTIME));
    }


    @Test
    void ensureReturnsFalseIfVacationTypeIsNotOfGivenCategory() {

        VacationType vacationType = new VacationType();
        vacationType.setCategory(VacationCategory.HOLIDAY);

        Assert.assertFalse("Categories should not match", vacationType.isOfCategory(VacationCategory.OVERTIME));
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
