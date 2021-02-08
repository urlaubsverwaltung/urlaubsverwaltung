package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypes;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsTest {

    @Mock
    private VacationTypeService vacationTypeService;

    @Test
    void ensureThrowsIfInitializedWithNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ApplicationForLeaveStatistics(null, null));
    }

    @Test
    void ensureHasDefaultValues() {

        final List<VacationType> vacationTypes = createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);

        // Total
        assertThat(statistics.getTotalWaitingVacationDays()).isEqualTo(ZERO);
        assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(ZERO);

        // Left
        assertThat(statistics.getLeftVacationDays()).isEqualTo(ZERO);
        assertThat(statistics.getLeftOvertime()).isEqualTo(Duration.ZERO);

        // Per vacation type
        assertThat(statistics.getWaitingVacationDays()).hasSize(createVacationTypes().size());
        assertThat(statistics.getAllowedVacationDays()).hasSize(createVacationTypes().size());

        for (VacationType type : createVacationTypes()) {
            assertThat(statistics.getWaitingVacationDays()).containsEntry(type, ZERO);
            assertThat(statistics.getAllowedVacationDays()).containsEntry(type, ZERO);
        }
    }

    // Total left vacation days ----------------------------------------------------------------------------------------
    @Test
    void ensureCanSetTotalLeftVacationDays() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);
        statistics.setLeftVacationDays(ONE);

        assertThat(statistics.getLeftVacationDays()).isEqualByComparingTo(ONE);
    }

    @Test
    void ensureThrowsIfSettingTotalLeftVacationDaysToNull() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);

        assertThatIllegalArgumentException().isThrownBy(() -> statistics.setLeftVacationDays(null));
    }

    // Adding vacation days --------------------------------------------------------------------------------------------
    @Test
    void ensureThrowsIfAddingWaitingVacationDaysWithNullVacationType() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);

        assertThatIllegalArgumentException().isThrownBy(() -> statistics.addWaitingVacationDays(null, ONE));
    }


    @Test
    void ensureThrowsIfAddingWaitingVacationDaysWithNullDays() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> statistics.addWaitingVacationDays(createVacationType(HOLIDAY), null));
    }

    @Test
    void ensureThrowsIfAddingAllowedVacationDaysWithNullVacationType() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> statistics.addAllowedVacationDays(null, ONE));
    }

    @Test
    void ensureThrowsIfAddingAllowedVacationDaysWithNullDays() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> statistics.addAllowedVacationDays(createVacationType(HOLIDAY), null));
    }

    @Test
    void ensureCanAddWaitingVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);

        assertThat(statistics.getWaitingVacationDays())
            .containsEntry(vacationTypes.get(0), new BigDecimal("2"))
            .containsEntry(vacationTypes.get(1), ONE)
            .containsEntry(vacationTypes.get(2), ZERO)
            .containsEntry(vacationTypes.get(3), ZERO);
    }

    @Test
    void ensureCanAddAllowedVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);
        statistics.addAllowedVacationDays(vacationTypes.get(2), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(2), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(3), ONE);

        assertThat(statistics.getAllowedVacationDays())
            .containsEntry(vacationTypes.get(0), ZERO)
            .containsEntry(vacationTypes.get(1), ZERO)
            .containsEntry(vacationTypes.get(2), new BigDecimal("2"))
            .containsEntry(vacationTypes.get(3), ONE);
    }

    // Total waiting vacation days -------------------------------------------------------------------------------------
    @Test
    void ensureCanCalculateTotalWaitingVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(2), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(3), BigDecimal.TEN);

        assertThat(statistics.getTotalWaitingVacationDays()).isEqualByComparingTo(BigDecimal.valueOf(16));
    }

    // Total allowed vacation days -------------------------------------------------------------------------------------
    @Test
    void ensureCanCalculateTotalAllowedVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);
        statistics.addAllowedVacationDays(vacationTypes.get(0), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(0), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(0), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(1), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(1), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(2), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(3), BigDecimal.TEN);

        assertThat(statistics.getTotalAllowedVacationDays()).isEqualByComparingTo(BigDecimal.valueOf(16));
    }

    // Total left overtime ---------------------------------------------------------------------------------------------
    @Test
    void ensureCanSetTotalLeftOvertime() {
        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);
        statistics.setLeftOvertime(Duration.ofHours(1));
        assertThat(statistics.getLeftOvertime()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void ensureThrowsIfSettingTotalLeftOvertimeToNull() {
        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationTypeService);

        assertThatIllegalArgumentException().isThrownBy(() -> statistics.setLeftOvertime(null));
    }
}
