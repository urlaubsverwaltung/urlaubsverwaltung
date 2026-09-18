package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriod;
import org.synyx.urlaubsverwaltung.department.Department;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createDepartment;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.blackoutperiod.web.BlackoutPeriodFormMapper.mapToBlackoutPeriod;
import static org.synyx.urlaubsverwaltung.blackoutperiod.web.BlackoutPeriodFormMapper.mapToForm;

class BlackoutPeriodFormMapperTest {

    private final StaticMessageSource messageSource = new StaticMessageSource();

    @Test
    void ensureMapToFormMapsDepartmentAndVacationTypeIds() {

        final Department department = createDepartment("Vertrieb");
        department.setId(42L);

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, messageSource);

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(7L);
        blackoutPeriod.setTitle("Jahresabschluss");
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));
        blackoutPeriod.setDepartments(List.of(department));
        blackoutPeriod.setVacationTypes(List.of(vacationType));

        final BlackoutPeriodForm form = mapToForm(blackoutPeriod);

        assertThat(form.getId()).isEqualTo(7L);
        assertThat(form.getTitle()).isEqualTo("Jahresabschluss");
        assertThat(form.getStartDate()).isEqualTo(LocalDate.of(2026, 12, 20));
        assertThat(form.getEndDate()).isEqualTo(LocalDate.of(2027, 1, 5));
        assertThat(form.getDepartmentIds()).containsExactly(42L);
        assertThat(form.getVacationTypeIds()).containsExactly(1L);
    }

    @Test
    void ensureMapToFormOfCompanyWideBlackoutPeriodWithoutRestrictedVacationTypes() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setTitle("Jahresabschluss");

        final BlackoutPeriodForm form = mapToForm(blackoutPeriod);

        assertThat(form.getId()).isNull();
        assertThat(form.getDepartmentIds()).isEmpty();
        assertThat(form.getVacationTypeIds()).isEmpty();
    }

    @Test
    void ensureMapToBlackoutPeriodResolvesOnlySelectedDepartmentsAndVacationTypes() {

        final Department selectedDepartment = createDepartment("Vertrieb");
        selectedDepartment.setId(42L);
        final Department otherDepartment = createDepartment("Marketing");
        otherDepartment.setId(43L);

        final VacationType<?> selectedVacationType = createVacationType(1L, HOLIDAY, messageSource);
        final VacationType<?> otherVacationType = createVacationType(2L, SPECIALLEAVE, messageSource);

        final BlackoutPeriodForm form = new BlackoutPeriodForm();
        form.setId(7L);
        form.setTitle("Vertriebssperre");
        form.setStartDate(LocalDate.of(2026, 12, 20));
        form.setEndDate(LocalDate.of(2027, 1, 5));
        form.setDepartmentIds(List.of(42L));
        form.setVacationTypeIds(List.of(1L));

        final BlackoutPeriod blackoutPeriod = mapToBlackoutPeriod(form,
            List.of(selectedDepartment, otherDepartment), List.of(selectedVacationType, otherVacationType));

        assertThat(blackoutPeriod.getId()).isEqualTo(7L);
        assertThat(blackoutPeriod.getTitle()).isEqualTo("Vertriebssperre");
        assertThat(blackoutPeriod.getStartDate()).isEqualTo(LocalDate.of(2026, 12, 20));
        assertThat(blackoutPeriod.getEndDate()).isEqualTo(LocalDate.of(2027, 1, 5));
        assertThat(blackoutPeriod.getDepartments()).containsExactly(selectedDepartment);
        assertThat(blackoutPeriod.getVacationTypes()).containsExactly(selectedVacationType);
    }

    @Test
    void ensureMapToBlackoutPeriodWithoutSelectionIsCompanyWideAndAppliesToAllVacationTypes() {

        final Department department = createDepartment("Vertrieb");
        department.setId(42L);
        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, messageSource);

        final BlackoutPeriodForm form = new BlackoutPeriodForm();
        form.setTitle("Jahresabschluss");

        final BlackoutPeriod blackoutPeriod = mapToBlackoutPeriod(form, List.of(department), List.of(vacationType));

        assertThat(blackoutPeriod.isCompanyWide()).isTrue();
        assertThat(blackoutPeriod.appliesToAllVacationTypes()).isTrue();
    }
}
