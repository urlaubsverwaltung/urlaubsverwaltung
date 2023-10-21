package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
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
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, List.of());

        // Total
        assertThat(statistics.getTotalWaitingVacationDays()).isEqualTo(ZERO);
        assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(ZERO);

        // Left
        assertThat(statistics.getLeftVacationDaysForYear()).isEqualTo(ZERO);
        assertThat(statistics.getLeftOvertimeForYear()).isEqualTo(Duration.ZERO);

        // Per vacation type
        for (VacationType type : createVacationTypes(new StaticMessageSource())) {
            assertThat(statistics.getWaitingVacationDays(type)).isEqualTo(ZERO);
            assertThat(statistics.getAllowedVacationDays(type)).isEqualTo(ZERO);
        }
    }

    @Test
    void ensureCanSetTotalLeftVacationDays() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, List.of());
        statistics.setLeftVacationDaysForYear(ONE);

        assertThat(statistics.getLeftVacationDaysForYear()).isEqualByComparingTo(ONE);
    }

    @Test
    void ensureCanAddWaitingVacationDays() {
        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypes);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);

        assertThat(statistics.getWaitingVacationDays())
            .containsEntry(vacationTypes.get(0), new BigDecimal("2"))
            .containsEntry(vacationTypes.get(1), ONE);
    }

    @Test
    void ensureHasVacationTypeIsTrueByWaiting() {
        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypes);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);

        assertThat(statistics.hasVacationType(vacationTypes.get(1))).isTrue();
    }

    @Test
    void ensureHasVacationTypeIsTrueByAllowed() {
        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypes);
        statistics.addAllowedVacationDays(vacationTypes.get(1), ONE);

        assertThat(statistics.hasVacationType(vacationTypes.get(1))).isTrue();
    }

    @Test
    void ensureHasVacationTypeIsTrueByAllowedAndWaiting() {
        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypes);
        statistics.addAllowedVacationDays(vacationTypes.get(1), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);

        assertThat(statistics.hasVacationType(vacationTypes.get(1))).isTrue();
    }

    @Test
    void ensureCanAddAllowedVacationDays() {
        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypes);
        statistics.addAllowedVacationDays(vacationTypes.get(2), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(2), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(3), ONE);

        assertThat(statistics.getAllowedVacationDays())
            .containsEntry(vacationTypes.get(2), new BigDecimal("2"))
            .containsEntry(vacationTypes.get(3), ONE);
    }

    @Test
    void ensureCanCalculateTotalWaitingVacationDays() {
        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypes);
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
        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypes);
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
        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, List.of());
        statistics.setLeftOvertimeForYear(Duration.ofHours(1));
        assertThat(statistics.getLeftOvertimeForYear()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void ensureReturnsOptionalEmptyForMissingPersonBasedata() {
        final ApplicationForLeaveStatistics applicationForLeaveStatistics = new ApplicationForLeaveStatistics(null, List.of());
        assertThat(applicationForLeaveStatistics.getPersonBasedata()).isEmpty();
    }
}
