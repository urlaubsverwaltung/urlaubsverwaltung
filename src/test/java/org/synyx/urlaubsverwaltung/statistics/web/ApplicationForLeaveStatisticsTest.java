package org.synyx.urlaubsverwaltung.statistics.web;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.statistics.ApplicationForLeaveStatistics;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypes;

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

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);

        // Total
        Assert.assertEquals("Total waiting vacation days should have default value", ZERO,
            statistics.getTotalWaitingVacationDays());
        Assert.assertEquals("Total allowed vacation days should have default value", ZERO,
            statistics.getTotalAllowedVacationDays());

        // Left
        Assert.assertEquals("Left vacation days should have default value", ZERO,
            statistics.getLeftVacationDays());
        Assert.assertEquals("Left overtime should have default value", ZERO, statistics.getLeftOvertime());

        // Per vacation type
        Assert.assertEquals("Wrong number of elements", createVacationTypes().size(),
            statistics.getWaitingVacationDays().size());
        Assert.assertEquals("Wrong number of elements", createVacationTypes().size(),
            statistics.getAllowedVacationDays().size());

        for (VacationType type : createVacationTypes()) {
            Assert.assertEquals("Waiting vacation days for " + type.getCategory() + " should be zero", ZERO,
                statistics.getWaitingVacationDays().get(type));
            Assert.assertEquals("Allowed vacation days for " + type.getCategory() + " should be zero", ZERO,
                statistics.getAllowedVacationDays().get(type));
        }
    }

    // Total left vacation days ----------------------------------------------------------------------------------------
    @Test
    void ensureCanSetTotalLeftVacationDays() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);
        statistics.setLeftVacationDays(ONE);

        assertThat(statistics.getLeftVacationDays()).isEqualByComparingTo(ONE);
    }

    @Test
    void ensureThrowsIfSettingTotalLeftVacationDaysToNull() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);

        assertThatIllegalArgumentException().isThrownBy(() -> statistics.setLeftVacationDays(null));
    }

    // Adding vacation days --------------------------------------------------------------------------------------------
    @Test
    void ensureThrowsIfAddingWaitingVacationDaysWithNullVacationType() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);

        assertThatIllegalArgumentException().isThrownBy(() -> statistics.addWaitingVacationDays(null, ONE));
    }


    @Test
    void ensureThrowsIfAddingWaitingVacationDaysWithNullDays() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> statistics.addWaitingVacationDays(createVacationType(HOLIDAY), null));
    }

    @Test
    void ensureThrowsIfAddingAllowedVacationDaysWithNullVacationType() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> statistics.addAllowedVacationDays(null, ONE));
    }

    @Test
    void ensureThrowsIfAddingAllowedVacationDaysWithNullDays() {
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> statistics.addAllowedVacationDays(createVacationType(HOLIDAY), null));
    }

    @Test
    void ensureCanAddWaitingVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(0), ONE);
        statistics.addWaitingVacationDays(vacationTypes.get(1), ONE);

        Assert.assertEquals("Wrong number of days", new BigDecimal("2"), statistics.getWaitingVacationDays().get(vacationTypes.get(0)));
        Assert.assertEquals("Wrong number of days", ONE, statistics.getWaitingVacationDays().get(vacationTypes.get(1)));
        Assert.assertEquals("Wrong number of days", ZERO, statistics.getWaitingVacationDays().get(vacationTypes.get(2)));
        Assert.assertEquals("Wrong number of days", ZERO, statistics.getWaitingVacationDays().get(vacationTypes.get(3)));
    }

    @Test
    void ensureCanAddAllowedVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);
        statistics.addAllowedVacationDays(vacationTypes.get(2), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(2), ONE);
        statistics.addAllowedVacationDays(vacationTypes.get(3), ONE);

        Assert.assertEquals("Wrong number of days", ZERO, statistics.getAllowedVacationDays().get(vacationTypes.get(0)));
        Assert.assertEquals("Wrong number of days", ZERO, statistics.getAllowedVacationDays().get(vacationTypes.get(1)));
        Assert.assertEquals("Wrong number of days", new BigDecimal("2"), statistics.getAllowedVacationDays().get(vacationTypes.get(2)));
        Assert.assertEquals("Wrong number of days", ONE, statistics.getAllowedVacationDays().get(vacationTypes.get(3)));
    }

    // Total waiting vacation days -------------------------------------------------------------------------------------
    @Test
    void ensureCanCalculateTotalWaitingVacationDays() {
        final List<VacationType> vacationTypes = createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);
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

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);
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
        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);
        statistics.setLeftOvertime(ONE);
        assertThat(statistics.getLeftOvertime()).isEqualByComparingTo(ONE);
    }

    @Test
    void ensureThrowsIfSettingTotalLeftOvertimeToNull() {
        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(createPerson(), vacationTypeService);

        assertThatIllegalArgumentException().isThrownBy(() -> statistics.setLeftOvertime(null));
    }
}
