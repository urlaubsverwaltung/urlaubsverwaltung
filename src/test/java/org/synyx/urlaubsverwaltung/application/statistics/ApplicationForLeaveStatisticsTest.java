package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypes;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsTest {

    @Test
    void ensureReturnsDefaultValuesForNotUsedVacationType() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);

        // Total
        assertThat(statistics.getTotalWaitingVacationDays()).isEqualTo(ZERO);
        assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(ZERO);

        // Left
        assertThat(statistics.getLeftVacationDays()).isEqualTo(ZERO);
        assertThat(statistics.getLeftOvertime()).isEqualTo(Duration.ZERO);

        // Per vacation type
        for (VacationType type : createVacationTypes()) {
            assertThat(statistics.getWaitingVacationDays(type)).isEqualTo(ZERO);
            assertThat(statistics.getAllowedVacationDays(type)).isEqualTo(ZERO);
        }
    }

    @Test
    void ensureCanSetTotalLeftVacationDays() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);
        statistics.setLeftVacationDays(ONE);

        assertThat(statistics.getLeftVacationDays()).isEqualByComparingTo(ONE);
    }

    @Test
    void ensureCanAddWaitingVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);

        assertThat(statistics.getWaitingVacationDays())
            .containsEntry(vacationTypes.get(0), new BigDecimal("2"))
            .containsEntry(vacationTypes.get(1), ONE);
    }

    @Test
    void ensureHasVacationTypeIsTrueByWaiting() {
        final List<VacationType> vacationTypes = createVacationTypes();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);

        assertThat(statistics.hasVacationType(vacationTypes.get(1))).isTrue();
    }

    @Test
    void ensureHasVacationTypeIsTrueByAllowed() {
        final List<VacationType> vacationTypes = createVacationTypes();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);
        statistics.addAllowedVacationDays(vacationTypes.get(1), ONE);

        assertThat(statistics.hasVacationType(vacationTypes.get(1))).isTrue();
    }

    @Test
    void ensureHasVacationTypeIsTrueByAllowedAndWaiting() {
        final List<VacationType> vacationTypes = createVacationTypes();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);
        statistics.addAllowedVacationDays(vacationTypes.get(1), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);

        assertThat(statistics.hasVacationType(vacationTypes.get(1))).isTrue();
    }

    @Test
    void ensureCanAddAllowedVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);
        statistics.addAllowedVacationDays(vacationTypes.get(2), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(2), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(3), ONE);

        assertThat(statistics.getAllowedVacationDays())
            .containsEntry(vacationTypes.get(2), new BigDecimal("2"))
            .containsEntry(vacationTypes.get(3), ONE);
    }

    @Test
    void ensureCanCalculateTotalWaitingVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(2), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(3), BigDecimal.TEN);

        assertThat(statistics.getTotalWaitingVacationDays()).isEqualByComparingTo(BigDecimal.valueOf(16));
    }

    @Test
    void ensureCanCalculateTotalAllowedVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);
        statistics.addAllowedVacationDays(vacationTypes.get(0), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(0), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(0), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(1), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(1), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(2), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(3), BigDecimal.TEN);

        assertThat(statistics.getTotalAllowedVacationDays()).isEqualByComparingTo(BigDecimal.valueOf(16));
    }

    @Test
    void ensureCanSetTotalLeftOvertime() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);
        statistics.setLeftOvertime(Duration.ofHours(1));
        assertThat(statistics.getLeftOvertime()).isEqualTo(Duration.ofHours(1));
    }
}
