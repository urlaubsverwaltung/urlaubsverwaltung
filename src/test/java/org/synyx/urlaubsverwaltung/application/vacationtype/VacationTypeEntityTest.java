package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

class VacationTypeEntityTest {

    @Test
    void ensureVacationTypeIsActive() {

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setActive(true);

        assertThat(vacationType.isActive()).isTrue();
    }

    @Test
    void ensureVacationTypeIsInactive() {

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setActive(false);

        assertThat(vacationType.isActive()).isFalse();
    }

    @Test
    void ensureReturnsTrueIfVacationTypeIsOfGivenCategory() {

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(OVERTIME);

        assertThat(vacationType.isOfCategory(OVERTIME)).isTrue();
    }

    @Test
    void ensureReturnsFalseIfVacationTypeIsNotOfGivenCategory() {

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(HOLIDAY);

        assertThat(vacationType.isOfCategory(OVERTIME)).isFalse();
    }

    @Test
    void toStringTest() {
        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(HOLIDAY);
        vacationType.setMessageKey("messageKey");
        vacationType.setId(10L);
        vacationType.setActive(true);
        vacationType.setRequiresApprovalToApply(false);
        vacationType.setRequiresApprovalToCancel(true);
        vacationType.setColor(YELLOW);

        final String vacationTypeToString = vacationType.toString();
        assertThat(vacationTypeToString).isEqualTo("VacationTypeEntity{id=10, active=true, category=HOLIDAY, requiresApprovalToApply=false, requiresApprovalToCancel=true, color=YELLOW, visibleToEveryone=false, custom=false, messageKey=messageKey, labelByLocale=null}");
    }

    @Test
    void equals() {
        final VacationTypeEntity vacationTypeOne = new VacationTypeEntity();
        vacationTypeOne.setId(1L);

        final VacationTypeEntity vacationTypeOneOne = new VacationTypeEntity();
        vacationTypeOneOne.setId(1L);

        final VacationTypeEntity vacationTypeTwo = new VacationTypeEntity();
        vacationTypeTwo.setId(2L);

        assertThat(vacationTypeOne)
            .isEqualTo(vacationTypeOne)
            .isEqualTo(vacationTypeOneOne)
            .isNotEqualTo(vacationTypeTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final VacationTypeEntity vacationTypeOne = new VacationTypeEntity();
        vacationTypeOne.setId(1L);

        assertThat(vacationTypeOne.hashCode()).isEqualTo(32);
    }
}
