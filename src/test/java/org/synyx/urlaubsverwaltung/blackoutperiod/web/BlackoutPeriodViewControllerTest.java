package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriod;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createDepartment;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

@ExtendWith(MockitoExtension.class)
class BlackoutPeriodViewControllerTest {

    private BlackoutPeriodViewController sut;

    @Mock
    private BlackoutPeriodService blackoutPeriodService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    private final BlackoutPeriodFormValidator validator = new BlackoutPeriodFormValidator();
    private final StaticMessageSource messageSource = new StaticMessageSource();

    @BeforeEach
    void setUp() {
        messageSource.setUseCodeAsDefaultMessage(true);
        sut = new BlackoutPeriodViewController(blackoutPeriodService, departmentService, vacationTypeService, validator,
            messageSource, defaultPersonSuggestionUrlStrategy, personSearchUiFragmentSupplier);
    }

    @Nested
    class PersonSearch {

        @Test
        void personSearchUiFragmentSupplier() {
            assertThat(sut.personSearchUiFragmentSupplier()).isSameAs(personSearchUiFragmentSupplier);
        }

        @Test
        void returnsInjectedStrategy() {
            assertThat(sut.personSuggestionUrlStrategy()).isSameAs(defaultPersonSuggestionUrlStrategy);
        }
    }

    @Test
    void showAllBlackoutPeriodsAddsBlackoutPeriodsToModel() throws Exception {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(1L);
        blackoutPeriod.setTitle("Jahresabschluss");
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));
        when(blackoutPeriodService.getAllBlackoutPeriods()).thenReturn(List.of(blackoutPeriod));

        perform(get("/web/blackoutperiod"))
            .andExpect(view().name("blackoutperiod/blackout_period_list"))
            .andExpect(model().attributeExists("blackoutPeriods"));
    }

    @Test
    void newBlackoutPeriodFormAddsEmptyFormToModel() throws Exception {

        when(vacationTypeService.getActiveVacationTypes()).thenReturn(List.of());
        when(departmentService.getAllDepartments()).thenReturn(List.of());

        perform(get("/web/blackoutperiod/new"))
            .andExpect(view().name("blackoutperiod/blackout_period_form"))
            .andExpect(model().attributeExists("blackoutPeriod"));
    }

    @Test
    void createBlackoutPeriodWithoutConflictsSavesAndRedirects() throws Exception {

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of());
        when(departmentService.getAllDepartments()).thenReturn(List.of());
        when(blackoutPeriodService.findConflictingApplications(any(BlackoutPeriod.class))).thenReturn(List.of());

        final BlackoutPeriod savedBlackoutPeriod = new BlackoutPeriod();
        savedBlackoutPeriod.setId(1L);
        savedBlackoutPeriod.setTitle("Jahresabschluss");
        when(blackoutPeriodService.create(any(BlackoutPeriod.class))).thenReturn(savedBlackoutPeriod);

        perform(post("/web/blackoutperiod/new")
            .param("title", "Jahresabschluss")
            .param("startDate", "2026-12-20")
            .param("endDate", "2027-01-05"))
            .andExpect(redirectedUrl("/web/blackoutperiod"))
            .andExpect(flash().attribute("createdBlackoutPeriodTitle", "Jahresabschluss"));

        verify(blackoutPeriodService).create(any(BlackoutPeriod.class));
    }

    @Test
    void createBlackoutPeriodWithConflictsAndNoConfirmationRedisplaysFormWithoutSaving() throws Exception {

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of());
        when(departmentService.getAllDepartments()).thenReturn(List.of());
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(List.of());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application conflictingApplication = new Application();
        conflictingApplication.setPerson(person);
        conflictingApplication.setStartDate(LocalDate.of(2026, 12, 22));
        conflictingApplication.setEndDate(LocalDate.of(2026, 12, 23));
        conflictingApplication.setVacationType(createVacationType(1L, HOLIDAY, messageSource));

        when(blackoutPeriodService.findConflictingApplications(any(BlackoutPeriod.class))).thenReturn(List.of(conflictingApplication));

        perform(post("/web/blackoutperiod/new")
            .param("title", "Jahresabschluss")
            .param("startDate", "2026-12-20")
            .param("endDate", "2027-01-05"))
            .andExpect(view().name("blackoutperiod/blackout_period_form"))
            .andExpect(model().attribute("confirmRequired", true))
            .andExpect(model().attributeExists("conflictingApplications"));

        verify(blackoutPeriodService, never()).create(any(BlackoutPeriod.class));
    }

    @Test
    void createBlackoutPeriodWithConflictsAndConfirmationSavesAnyway() throws Exception {

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of());
        when(departmentService.getAllDepartments()).thenReturn(List.of());

        final BlackoutPeriod savedBlackoutPeriod = new BlackoutPeriod();
        savedBlackoutPeriod.setId(1L);
        savedBlackoutPeriod.setTitle("Jahresabschluss");
        when(blackoutPeriodService.create(any(BlackoutPeriod.class))).thenReturn(savedBlackoutPeriod);

        perform(post("/web/blackoutperiod/new")
            .param("title", "Jahresabschluss")
            .param("startDate", "2026-12-20")
            .param("endDate", "2027-01-05")
            .param("confirm", "true"))
            .andExpect(redirectedUrl("/web/blackoutperiod"));

        verify(blackoutPeriodService).create(any(BlackoutPeriod.class));
        verify(blackoutPeriodService, never()).findConflictingApplications(any(BlackoutPeriod.class));
    }

    @Test
    void createBlackoutPeriodWithoutTitleRedisplaysFormWithErrors() throws Exception {

        when(vacationTypeService.getActiveVacationTypes()).thenReturn(List.of());
        when(departmentService.getAllDepartments()).thenReturn(List.of());

        perform(post("/web/blackoutperiod/new")
            .param("startDate", "2026-12-20")
            .param("endDate", "2027-01-05"))
            .andExpect(view().name("blackoutperiod/blackout_period_form"));

        verify(blackoutPeriodService, never()).create(any(BlackoutPeriod.class));
    }

    @Test
    void deleteBlackoutPeriodDeletesAndRedirects() throws Exception {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(1L);
        blackoutPeriod.setTitle("Jahresabschluss");
        when(blackoutPeriodService.getBlackoutPeriodById(1L)).thenReturn(Optional.of(blackoutPeriod));

        perform(post("/web/blackoutperiod/1/delete"))
            .andExpect(redirectedUrl("/web/blackoutperiod"))
            .andExpect(flash().attribute("deletedBlackoutPeriodTitle", "Jahresabschluss"));

        verify(blackoutPeriodService).delete(1L);
    }


    @Test
    void showAllBlackoutPeriodsResolvesCompanyWideScopeAndAllVacationTypesLabels() throws Exception {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(1L);
        blackoutPeriod.setTitle("Jahresabschluss");
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));
        blackoutPeriod.setCompanyWide(true);
        blackoutPeriod.setAllVacationTypes(true);
        when(blackoutPeriodService.getAllBlackoutPeriods()).thenReturn(List.of(blackoutPeriod));

        perform(get("/web/blackoutperiod"))
            .andExpect(model().attribute("blackoutPeriods", List.of(new BlackoutPeriodListDto(1L, "Jahresabschluss",
                LocalDate.of(2026, 12, 20), LocalDate.of(2027, 1, 5),
                "blackoutperiod.scope.companyWide", "blackoutperiod.scope.allVacationTypes"))));
    }

    @Test
    void showAllBlackoutPeriodsResolvesDepartmentAndVacationTypeLabels() throws Exception {

        final Department vertrieb = createDepartment("Vertrieb");
        vertrieb.setId(42L);
        final Department marketing = createDepartment("Marketing");
        marketing.setId(43L);

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, messageSource);

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(1L);
        blackoutPeriod.setTitle("Vertriebssperre");
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));
        blackoutPeriod.setDepartments(List.of(vertrieb, marketing));
        blackoutPeriod.setVacationTypes(List.of(vacationType));
        when(blackoutPeriodService.getAllBlackoutPeriods()).thenReturn(List.of(blackoutPeriod));

        perform(get("/web/blackoutperiod"))
            .andExpect(model().attribute("blackoutPeriods", List.of(new BlackoutPeriodListDto(1L, "Vertriebssperre",
                LocalDate.of(2026, 12, 20), LocalDate.of(2027, 1, 5),
                "Vertrieb, Marketing", "application.data.vacationType.holiday"))));
    }

    @Test
    void newBlackoutPeriodFormAddsSelectableOptionsToModel() throws Exception {

        final Department department = createDepartment("Vertrieb");
        department.setId(42L);
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(List.of(createVacationType(1L, HOLIDAY, messageSource)));

        perform(get("/web/blackoutperiod/new"))
            .andExpect(model().attribute("departmentOptions", List.of(new BlackoutPeriodOptionDto(42L, "Vertrieb", false))))
            .andExpect(model().attribute("vacationTypeOptions",
                List.of(new BlackoutPeriodOptionDto(1L, "application.data.vacationType.holiday", false))));
    }

    @Test
    void editBlackoutPeriodFormAddsPrefilledFormAndPreselectedOptionsToModel() throws Exception {

        final Department department = createDepartment("Vertrieb");
        department.setId(42L);
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, messageSource);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(List.of(vacationType));

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(1L);
        blackoutPeriod.setTitle("Vertriebssperre");
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));
        blackoutPeriod.setDepartments(List.of(department));
        blackoutPeriod.setVacationTypes(List.of(vacationType));
        when(blackoutPeriodService.getBlackoutPeriodById(1L)).thenReturn(Optional.of(blackoutPeriod));

        perform(get("/web/blackoutperiod/1/edit"))
            .andExpect(view().name("blackoutperiod/blackout_period_form"))
            .andExpect(model().attribute("blackoutPeriod", hasProperty("id", is(1L))))
            .andExpect(model().attribute("blackoutPeriod", hasProperty("title", is("Vertriebssperre"))))
            .andExpect(model().attribute("departmentOptions", List.of(new BlackoutPeriodOptionDto(42L, "Vertrieb", true))))
            .andExpect(model().attribute("vacationTypeOptions",
                List.of(new BlackoutPeriodOptionDto(1L, "application.data.vacationType.holiday", true))));
    }

    @Test
    void editBlackoutPeriodFormThrowsForUnknownBlackoutPeriod() {

        when(blackoutPeriodService.getBlackoutPeriodById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perform(get("/web/blackoutperiod/1/edit")))
            .hasCauseInstanceOf(UnknownBlackoutPeriodException.class);
    }

    @Test
    void updateBlackoutPeriodAppliesPathIdAndRedirects() throws Exception {

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of());
        when(departmentService.getAllDepartments()).thenReturn(List.of());

        final BlackoutPeriod savedBlackoutPeriod = new BlackoutPeriod();
        savedBlackoutPeriod.setId(1L);
        savedBlackoutPeriod.setTitle("Jahresabschluss");
        when(blackoutPeriodService.update(any(BlackoutPeriod.class))).thenReturn(savedBlackoutPeriod);

        perform(post("/web/blackoutperiod/1")
            .param("title", "Jahresabschluss")
            .param("startDate", "2026-12-20")
            .param("endDate", "2027-01-05")
            .param("confirm", "true"))
            .andExpect(redirectedUrl("/web/blackoutperiod"))
            .andExpect(flash().attribute("updatedBlackoutPeriodTitle", "Jahresabschluss"));

        final ArgumentCaptor<BlackoutPeriod> captor = ArgumentCaptor.forClass(BlackoutPeriod.class);
        verify(blackoutPeriodService).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void deleteBlackoutPeriodDoesNothingForUnknownBlackoutPeriod() throws Exception {

        when(blackoutPeriodService.getBlackoutPeriodById(1L)).thenReturn(Optional.empty());

        perform(post("/web/blackoutperiod/1/delete"))
            .andExpect(redirectedUrl("/web/blackoutperiod"))
            .andExpect(flash().attributeCount(0));

        verify(blackoutPeriodService, never()).delete(any());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
