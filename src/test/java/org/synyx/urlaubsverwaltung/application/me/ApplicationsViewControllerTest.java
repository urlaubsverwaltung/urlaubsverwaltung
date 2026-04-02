package org.synyx.urlaubsverwaltung.application.me;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static java.math.BigDecimal.ONE;
import java.util.Locale;
import java.math.BigDecimal;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.me.ApplicationsViewController.MY_APPLICATIONS_ANONYMOUS_PATH;
import static org.synyx.urlaubsverwaltung.application.me.ApplicationsViewController.MY_APPLICATIONS_PATH;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;

@ExtendWith(MockitoExtension.class)
class ApplicationsViewControllerTest {

    private ApplicationsViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private VacationTypeViewModelService vacationTypeViewModelService;

    private final Clock clock = Clock.fixed(ZonedDateTime.of(LocalDate.of(2022, 6, 15).atStartOfDay(), ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        sut = new ApplicationsViewController(personService, departmentService, applicationService, workDaysCountService, vacationTypeViewModelService, clock);
    }

    @Test
    void showMyApplicationsAnonymousRedirectsToPersonApplicationsWithoutYear() throws Exception {
        final Person signedIn = new Person();
        signedIn.setId(5L);
        when(personService.getSignedInUser()).thenReturn(signedIn);

        perform(get(MY_APPLICATIONS_ANONYMOUS_PATH))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/persons/5/applications"));
    }

    @Test
    void showMyApplicationsAnonymousRedirectsToPersonApplicationsWithGivenYear() throws Exception {
        final Person signedIn = new Person();
        signedIn.setId(7L);
        when(personService.getSignedInUser()).thenReturn(signedIn);

        perform(get(MY_APPLICATIONS_ANONYMOUS_PATH).param("year", "2021"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/persons/7/applications?year=2021"));
    }

    @Test
    void showMyApplicationsForPersonShowsViewAndModelAttributesWhenNoApplications() throws Exception {
        final Person person = new Person();
        person.setId(13L);

        when(personService.getPersonByID(13L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(List.of());

        perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "13")))
            .andExpect(status().isOk())
            .andExpect(view().name("me/applications"))
            .andExpect(model().attribute("person", equalTo(person)))
            .andExpect(model().attribute("departmentsOfPerson", equalTo(List.of())))
            .andExpect(model().attribute("vacationTypeColors", hasSize(1)))
            .andExpect(model().attribute("applications", hasSize(0)));
    }

    @Test
    void showMyApplicationsForPersonWithApplicationsMapsToDto() throws Exception {
        final Person person = new Person();
        person.setId(21L);

        when(personService.getPersonByID(21L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of());
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        final Application application = new Application();
        application.setId(99L);
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2022, 1, 10));
        application.setEndDate(LocalDate.of(2022, 1, 12));

        final VacationType<?> vacationType = mock(VacationType.class);
        when(vacationType.getLabel(any(Locale.class))).thenReturn("label");
        when(vacationType.getCategory()).thenReturn(HOLIDAY);
        when(vacationType.getColor()).thenReturn(ORANGE);
        application.setVacationType(vacationType);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(List.of(application));
        when(workDaysCountService.getWorkDaysCount(any(), any(LocalDate.class), any(LocalDate.class), eq(person))).thenReturn(ONE);

         perform(get(MY_APPLICATIONS_PATH.replace("{personId}", "21")).locale(Locale.GERMANY))
            .andExpect(status().isOk())
            .andExpect(view().name("me/applications"))
            .andExpect(model().attribute("applications", hasSize(1)))
            .andExpect(model().attribute("applications",
                hasItem(hasProperty("id", is(99L)))));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
