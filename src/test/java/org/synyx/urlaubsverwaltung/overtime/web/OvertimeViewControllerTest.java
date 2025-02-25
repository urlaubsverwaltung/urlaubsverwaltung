package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.time.Duration.ofHours;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.CREATED;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class OvertimeViewControllerTest {

    private OvertimeViewController sut;

    @Mock
    private OvertimeService overtimeService;
    @Mock
    private PersonService personService;
    @Mock
    private OvertimeFormValidator validator;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private VacationTypeViewModelService vacationTypeViewModelService;
    @Mock
    private SettingsService settingsService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new OvertimeViewController(overtimeService, personService, validator, departmentService, applicationService, vacationTypeViewModelService, settingsService, clock);
    }

    @Test
    void postRecordOvertimeShowsFormIfValidationFails() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));

        final Person overtimePerson = new Person();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getPersonByID(1337L)).thenReturn(Optional.of(overtimePerson));
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, overtimePerson)).thenReturn(true);

        mockSettings();

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/overtime").param("person", "1337"))
            .andExpect(model().attribute("overtime", instanceOf(OvertimeForm.class)))
            .andExpect(model().attribute("person", overtimePerson))
            .andExpect(view().name("overtime/overtime_form"));

        verify(validator).validate(any(OvertimeForm.class), any(Errors.class));
    }

    @Test
    void postUpdateOvertimeShowsFormIfValidationFails() throws Exception {

        Person person = new Person();
        person.setId(1L);
        final Overtime overtime = new Overtime(person, LocalDate.of(2019, 7, 2), LocalDate.of(2019, 7, 2), ofHours(10));
        when(overtimeService.getOvertimeById(anyLong())).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, person)).thenReturn(true);

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "errors");
            return null;
        }).when(validator).validate(any(), any());

        mockSettings();

        perform(post("/web/overtime/5").param("person.id", "1"))
            .andExpect(model().attribute("overtime", instanceOf(OvertimeForm.class)))
            .andExpect(view().name("overtime/overtime_form"));
    }

    @Test
    void showPersonalOvertime() throws Exception {

        final Person person = new Person();
        person.setId(5L);
        when(personService.getSignedInUser()).thenReturn(person);

        perform(get("/web/overtime"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/overtime?person=5"));
    }

    @Test
    void showUsersOvertimeListAndIsAllowedToAddOvertime() throws Exception {

        final long personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);
        when(personService.getSignedInUser()).thenReturn(person);

        perform(get("/web/overtime").param("person", "5"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("userIsAllowedToWriteOvertime", is(true)));
    }

    @Test
    void showUsersOvertimeListAndIsNotAllowedToAddOvertime() throws Exception {

        final long personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(false);
        when(personService.getSignedInUser()).thenReturn(person);

        perform(get("/web/overtime").param("person", "5"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("userIsAllowedToWriteOvertime", is(false)));
    }

    @Test
    void showOvertimeIsAllowed() throws Exception {

        final long personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Department department = new Department();
        department.setName("Buchhaltung");
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(department));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, person)).thenReturn(true);

        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, person)).thenReturn(true);

        final LocalDate today = LocalDate.now(clock);
        final int year = today.getYear();
        final Overtime overtime = new Overtime(person, today, today, ofHours(10));
        overtime.setId(1L);

        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, year)).thenReturn(List.of(overtime));
        when(overtimeService.getTotalOvertimeForPersonAndYear(person, year)).thenReturn(ofHours(1));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ZERO);

        final VacationType<?> vacationType = createVacationType(1L, OVERTIME, new StaticMessageSource());
        final Application applicationNonEditable = createApplication(person, vacationType, today, today, FULL);
        applicationNonEditable.setHours(ofHours(8));

        final LocalDate firstDayOfYear = Year.of(year).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(firstDayOfYear, lastDayOfYear, person, activeStatuses(), OVERTIME))
            .thenReturn(List.of(applicationNonEditable));

        perform(get("/web/overtime").param("person", "5"))
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_list"))
            .andExpect(model().attribute("currentYear", is(Year.now(clock).getValue())))
            .andExpect(model().attribute("selectedYear", is(Year.now(clock).getValue())))
            .andExpect(model().attribute("person", is(person)))
            .andExpect(model().attribute("overtimeTotal", is(ofHours(1))))
            .andExpect(model().attribute("overtimeTotalLastYear", is(Duration.ZERO)))
            .andExpect(model().attribute("overtimeLeft", is(Duration.ZERO)))
            .andExpect(model().attribute("userIsAllowedToWriteOvertime", is(true)))
            .andExpect(model().attribute("records", hasItems(
                new OvertimeListRecordDto(overtime.getId(), overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getDurationByYear(), ofHours(10), "", "", "OVERTIME", true),
                new OvertimeListRecordDto(overtime.getId(), applicationNonEditable.getStartDate(), applicationNonEditable.getEndDate(), ofHours(-8), null, ofHours(2), "WAITING", "ORANGE", "ABSENCE", false)
            )));
    }

    @Test
    void showOvertimeIsAllowedWithYear() throws Exception {

        final int year = 2012;

        final long personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, person)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, person)).thenReturn(true);

        final Overtime overtime = new Overtime(person, LocalDate.of(2012, 2, 5), LocalDate.of(2012, 2, 5), ofHours(10));
        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, year)).thenReturn(List.of(overtime));

        when(overtimeService.getTotalOvertimeForPersonAndYear(person, year)).thenReturn(ofHours(1));
        when(overtimeService.getTotalOvertimeForPersonBeforeYear(person, year)).thenReturn(ofHours(10));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ZERO);

        final OvertimeListRecordDto listRecordDto = new OvertimeListRecordDto(overtime.getId(), overtime.getStartDate(),
            overtime.getEndDate(), overtime.getDuration(), overtime.getDurationByYear(), ofHours(20), "", "", "OVERTIME", true);

        perform(get("/web/overtime")
            .param("person", "5")
            .param("year", "2012"))
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_list"))
            .andExpect(model().attribute("currentYear", is(Year.now(clock).getValue())))
            .andExpect(model().attribute("selectedYear", is(2012)))
            .andExpect(model().attribute("person", is(person)))
            .andExpect(model().attribute("overtimeTotal", is(ofHours(1))))
            .andExpect(model().attribute("overtimeLeft", is(Duration.ZERO)))
            .andExpect(model().attribute("overtimeTotalLastYear", is(ofHours(10))))
            .andExpect(model().attribute("records", hasItem(listRecordDto)));
    }

    @Test
    void showOvertimeIsNotAllowed() {

        final Person person = new Person();
        person.setId(5L);
        when(personService.getPersonByID(5L)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, person)).thenReturn(false);

        assertThatThrownBy(() -> perform(get("/web/overtime").param("person", "5")))
            .hasCause(new AccessDeniedException("User '1' has not the correct permissions to see overtime records of user '5'"));
    }

    @Test
    void showOvertimeDetails() throws Exception {

        final Person overtimePerson = new Person();

        final long overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.of(2016, 2, 5);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2016, 2, 5), overtimeEndDate, ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Department department = new Department();
        department.setName("Buchhaltung");
        when(departmentService.getAssignedDepartmentsOfMember(overtimePerson)).thenReturn(List.of(department));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(overtimePerson, overtimePerson)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(true);

        final OvertimeComment comment = new OvertimeComment(overtimePerson, overtime, CREATED, Clock.systemUTC());
        final List<OvertimeComment> overtimeComments = List.of(comment);
        when(overtimeService.getCommentsForOvertime(overtime)).thenReturn(overtimeComments);

        when(overtimeService.getTotalOvertimeForPersonAndYear(overtimePerson, overtimeEndDate.getYear())).thenReturn(ofHours(1));
        when(overtimeService.getLeftOvertimeForPerson(overtimePerson)).thenReturn(Duration.ZERO);

        final OvertimeDetailPersonDto personDto = new OvertimeDetailPersonDto(overtimePerson.getId(), overtimePerson.getEmail(), overtimePerson.getNiceName(), overtimePerson.getInitials(), overtimePerson.getGravatarURL(), false);
        final OvertimeDetailRecordDto record = new OvertimeDetailRecordDto(overtimeId, personDto, overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getDurationByYear(), overtime.getLastModificationDate());
        final OvertimeCommentDto commentDto = new OvertimeCommentDto(new OvertimeCommentPersonDto(comment.getPerson().getId(), comment.getPerson().getNiceName(), comment.getPerson().getInitials(), comment.getPerson().getGravatarURL()), comment.getAction().toString(), comment.getDate(), comment.getText());

        perform(get("/web/overtime/2"))
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_details"))
            .andExpect(model().attribute("currentYear", is(Year.now(clock).getValue())))
            .andExpect(model().attribute("overtimeTotal", is(ofHours(1))))
            .andExpect(model().attribute("overtimeLeft", is(Duration.ZERO)))
            .andExpect(model().attribute("userIsAllowedToWriteOvertime", is(true)))
            .andExpect(model().attribute("record", is(record)))
            .andExpect(model().attribute("comments", hasItem(commentDto)))
            .andExpect(model().attribute("departmentsOfPerson", List.of(department)));
    }

    @Test
    void showOvertimeDetailsIsNotAllowed() {

        final Person overtimePerson = new Person();
        overtimePerson.setId(5L);

        final long overtimeId = 2;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2016, 2, 5), LocalDate.of(2016, 2, 5), ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, overtimePerson)).thenReturn(false);

        assertThatThrownBy(() -> perform(get("/web/overtime/2")))
            .hasCause(new AccessDeniedException("User '1' has not the correct permissions to see overtime records of user '5'"));
    }

    @Test
    void recordOvertimeSignedInUserSame() throws Exception {

        final long personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, VacationTypeColor.ORANGE)));

        mockSettings();

        final ResultActions resultActions = perform(get("/web/overtime/new").param("person", "5"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_form"))
            .andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))))
            .andExpect(model().attribute("person", is(person)))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, VacationTypeColor.ORANGE)))));
    }

    @Test
    void recordOvertimeSignedInUserSameButOnlyPrivilegedAreAllowed() {

        final Person person = new Person();
        person.setId(5L);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(5L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(false);

        assertThatThrownBy(() -> perform(get("/web/overtime/new").param("person", "5")))
            .hasCause(new AccessDeniedException("User '5' has not the correct permissions to record overtime for user '5'"));
    }

    @Test
    void recordOvertimePersonIdIsNull() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);

        mockSettings();

        final ResultActions resultActions = perform(get("/web/overtime/new"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_form"))
            .andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))));
    }

    @Test
    void recordOvertimeSignedInUserIsNotSame() {

        final Person person = new Person();
        person.setId(5L);
        when(personService.getPersonByID(5L)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        assertThatThrownBy(() -> perform(get("/web/overtime/new").param("person", "5")))
            .hasCause(new AccessDeniedException("User '1' has not the correct permissions to record overtime for user '5'"));
    }

    @Test
    void recordOvertimeSignedInUserIsNotSameButOffice() throws Exception {

        final Person overtimePerson = new Person();
        final long overtimePersonId = 1;
        overtimePerson.setId(overtimePersonId);

        final Person signedInPerson = new Person();
        signedInPerson.setId(2L);
        signedInPerson.setPermissions(List.of(OFFICE));

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        final List<Person> activePersons = List.of(signedInPerson, overtimePerson);
        when(personService.getActivePersons()).thenReturn(activePersons);
        when(personService.getPersonByID(overtimePersonId)).thenReturn(Optional.of(overtimePerson));
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, overtimePerson)).thenReturn(true);

        mockSettings();

        final ResultActions resultActions = perform(get("/web/overtime/new").param("person", "1"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_form"))
            .andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))))
            .andExpect(model().attribute("persons", is(activePersons)));
    }

    @Test
    void recordOvertimeSignedInUserIsNotSameButPrivileged() throws Exception {

        final Person overtimePerson = new Person();
        final long overtimePersonId = 1;
        overtimePerson.setId(overtimePersonId);

        final Person signedInPerson = new Person();
        signedInPerson.setId(2L);
        signedInPerson.setPermissions(List.of(DEPARTMENT_HEAD));

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        final List<Person> activePersons = List.of(signedInPerson, overtimePerson);
        when(departmentService.getManagedActiveMembersOfPerson(signedInPerson)).thenReturn(activePersons);
        when(personService.getPersonByID(overtimePersonId)).thenReturn(Optional.of(overtimePerson));
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, overtimePerson)).thenReturn(true);

        mockSettings();

        final ResultActions resultActions = perform(get("/web/overtime/new").param("person", "1"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_form"))
            .andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))))
            .andExpect(model().attribute("persons", is(activePersons)));
    }

    @Test
    void editOvertime() throws Exception {

        final Person overtimePerson = new Person();

        final long overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.of(2016, 2, 5);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2016, 2, 5), overtimeEndDate, ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));
        when(personService.getSignedInUser()).thenReturn(overtimePerson);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(true);
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, VacationTypeColor.ORANGE)));

        mockSettings();

        final ResultActions resultActions = perform(get("/web/overtime/2/edit"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_form"))
            .andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))))
            .andExpect(model().attribute("person", is(overtimePerson)))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, VacationTypeColor.ORANGE)))));
    }

    @Test
    void editOvertimeSignedInUserSameButOnlyPrivilegedAreAllowed() {

        final Person overtimePerson = new Person();
        overtimePerson.setId(5L);
        overtimePerson.setPermissions(List.of(USER));

        final long overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.of(2016, 2, 5);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2016, 2, 5), overtimeEndDate, ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));
        when(personService.getSignedInUser()).thenReturn(overtimePerson);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(false);

        assertThatThrownBy(() -> perform(get("/web/overtime/2/edit")))
            .hasCause(new AccessDeniedException("User '5' has not the correct permissions to edit overtime record of user '5'"));
    }

    @Test
    void editOvertimeDifferentPersons() {

        final Person overtimePerson = new Person();
        overtimePerson.setId(5L);

        final long overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.of(2016, 2, 5);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2016, 2, 5), overtimeEndDate, ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        assertThatThrownBy(() -> perform(get("/web/overtime/2/edit")))
            .hasCause(new AccessDeniedException("User '1' has not the correct permissions to edit overtime record of user '5'"));
    }

    @Test
    void editOvertimeDifferentPersonsButOffice() throws Exception {

        final Person overtimePerson = new Person();

        final long overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.of(2016, 2, 5);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2016, 2, 5), overtimeEndDate, ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, overtimePerson)).thenReturn(true);

        mockSettings();

        perform(get("/web/overtime/2/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_form"))
            .andExpect(model().attribute("canAddOvertimeForAnotherUser", true));
    }

    @Test
    void ensureNewOvertimeDoesNotShowReductionWhenFeatureIsDisabled() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);

        mockSettingsWithOvertimeReductionDisabled();

        final ResultActions resultActions = perform(get("/web/overtime/new"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("overtimeReductionPossible", is(false)));
    }

    @Test
    void createOvertimeRecord() throws Exception {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2019, 7, 2), LocalDate.of(2019, 7, 2), ofHours(10));
        overtime.setId(2L);
        when(overtimeService.save(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(true);

        final ResultActions resultActions = perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        );

        resultActions
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/overtime/2"))
            .andExpect(flash().attribute("overtimeRecord", "CREATED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"25.03.2022", "25.03.22", "25.3.2022", "25.3.22", "1.4.22"})
    void ensureCreateOvertimeRecordSucceedsWithDateFormat(String givenDate) throws Exception {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2022, 3, 1), LocalDate.of(2022, 4, 28), ofHours(10));
        overtime.setId(2L);
        when(overtimeService.save(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(true);

        final ResultActions resultActions = perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", givenDate)
                .param("endDate", givenDate)
                .param("hours", "8")
                .param("comment", "To much work")
        );

        resultActions
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/overtime/2"))
            .andExpect(flash().attribute("overtimeRecord", "CREATED"));
    }

    @Test
    void ensureCreateOvertimeValidationErrorPageDoesNotShowReductionWhenFeatureIsDisabled() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, VacationTypeColor.ORANGE)));

        mockSettingsWithOvertimeReductionDisabled();

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "errors");
            return null;
        }).when(validator).validate(any(), any());

        final ResultActions resultActions = perform(
            post("/web/overtime")
                .param("person.id", "1")
                .param("startDate", "02.07.2021")
                .param("endDate", "02.07.2021")
                .param("hours", "8")
                .param("reduce", "true")
        );

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("overtimeReductionPossible", is(false)))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, VacationTypeColor.ORANGE)))));
    }

    @Test
    void createOvertimeRecordButOnlyPrivilegedAreAllowed() {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);
        overtimePerson.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(overtimePerson);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(false);

        assertThatThrownBy(() -> perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        )).hasCause(new AccessDeniedException("User '4' has not the correct permissions to record overtime for user '4'"));
    }

    @Test
    void ensureOvertimeHoursMustBeGreaterZero() throws Exception {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(true);

        mockSettings();

        final ResultActions resultActions = perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "18.12.2020")
                .param("endDate", "18.12.2020")
                .param("hours", "-8")
        );

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("overtime", "hours"))
            .andExpect(view().name("overtime/overtime_form"));
    }

    @Test
    void ensureOvertimeMinutesMustBeGreaterZero() throws Exception {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(true);

        mockSettings();

        final ResultActions resultActions = perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "18.12.2020")
                .param("endDate", "18.12.2020")
                .param("minutes", "-30")
        );

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("overtime", "minutes"))
            .andExpect(view().name("overtime/overtime_form"));
    }

    @Test
    void createOvertimeRecordNotSamePerson() {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);

        assertThatThrownBy(() -> perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        )).hasCause(new AccessDeniedException("User '1' has not the correct permissions to record overtime for user '4'"));
    }

    @Test
    void createOvertimeRecordNotSamePersonButOffice() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);

        final Overtime overtime = new Overtime(signedInPerson, LocalDate.of(2019, 7, 2), LocalDate.of(2019, 7, 2), ofHours(10));
        overtime.setId(2L);
        when(overtimeService.save(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, overtimePerson)).thenReturn(true);

        final ResultActions resultActions = perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        );

        resultActions
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/overtime/2"))
            .andExpect(flash().attribute("overtimeRecord", "CREATED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"25.03.2022", "25.03.22", "25.3.2022", "25.3.22", "1.4.22"})
    void updateOvertimeWithDateSucceeds(String givenDate) throws Exception {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2022, 3, 1), LocalDate.of(2022, 4, 28), ofHours(10));
        overtime.setId(2L);
        when(overtimeService.getOvertimeById(2L)).thenReturn(Optional.of(overtime));
        when(overtimeService.save(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(true);

        final ResultActions resultActions = perform(
            post("/web/overtime/2")
                .param("id", "2")
                .param("person.id", "4")
                .param("startDate", givenDate)
                .param("endDate", givenDate)
                .param("hours", "8")
                .param("comment", "To much work")
        );

        resultActions
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/overtime/2"))
            .andExpect(flash().attribute("overtimeRecord", "EDITED"));
    }

    @Test
    void ensureUpdateOvertimePageDoesNotShowReductionWhenFeatureIsDisabled() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);

        final Overtime overtime = new Overtime(person, LocalDate.of(2019, 7, 2), LocalDate.of(2019, 7, 2), ofHours(8));
        overtime.setId(2L);
        when(overtimeService.getOvertimeById(2L)).thenReturn(Optional.of(overtime));

        mockSettingsWithOvertimeReductionDisabled();

        final ResultActions resultActions = perform(get("/web/overtime/2/edit"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("overtimeReductionPossible", is(false)));
    }

    @Test
    void ensureUpdateOvertimeValidationErrorPageDoesNotShowReductionWhenFeatureIsDisabled() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final Overtime overtime = new Overtime(person, LocalDate.of(2021, 7, 2), LocalDate.of(2021, 7, 2), ofHours(8));
        overtime.setId(2L);
        when(overtimeService.getOvertimeById(2L)).thenReturn(Optional.of(overtime));
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, VacationTypeColor.ORANGE)));

        mockSettingsWithOvertimeReductionDisabled();

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "errors");
            return null;
        }).when(validator).validate(any(), any());

        final ResultActions resultActions = perform(
            post("/web/overtime/2")
                .param("person.id", "1")
                .param("startDate", "02.07.2021")
                .param("endDate", "02.07.2021")
                .param("hours", "8")
                .param("reduce", "true")
        );
        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("overtimeReductionPossible", is(false)))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, VacationTypeColor.ORANGE)))));
    }

    @Test
    void updateOvertimeRecordButOnlyPrivilegedAreAllowed() {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);
        overtimePerson.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2019, 7, 2), LocalDate.of(2019, 7, 2), ofHours(10));
        overtime.setId(2L);
        when(overtimeService.getOvertimeById(2L)).thenReturn(Optional.of(overtime));
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(false);

        assertThatThrownBy(() -> perform(
            post("/web/overtime/2")
                .param("id", "2")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        )).hasCause(new AccessDeniedException("User '4' has not the correct permissions to edit overtime record of user '4'"));
    }

    @Test
    void updateOvertimeIsNotSamePerson() {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2019, 7, 2), LocalDate.of(2019, 7, 2), ofHours(10));
        overtime.setId(2L);
        when(overtimeService.getOvertimeById(2L)).thenReturn(Optional.of(overtime));

        assertThatThrownBy(() -> perform(
            post("/web/overtime/2")
                .param("id", "2")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        )).hasCause(new AccessDeniedException("User '1' has not the correct permissions to edit overtime record of user '4'"));
    }

    @Test
    void updateOvertimeIsNotSamePersonButOffice() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2019, 7, 2), LocalDate.of(2019, 7, 2), ofHours(10));
        overtime.setId(2L);
        when(overtimeService.getOvertimeById(2L)).thenReturn(Optional.of(overtime));
        when(overtimeService.save(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, overtimePerson)).thenReturn(true);

        final ResultActions resultActions = perform(
            post("/web/overtime/2")
                .param("id", "2")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        );

        resultActions
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/overtime/2"))
            .andExpect(flash().attribute("overtimeRecord", "EDITED"));
    }

    @Test
    void updateOvertimeRecordAsOfficeChangingOvertimePerson() {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4L);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.of(2019, 7, 2), LocalDate.of(2019, 7, 2), ofHours(10));
        overtime.setId(2L);
        when(overtimeService.getOvertimeById(2L)).thenReturn(Optional.of(overtime));

        final String otherPersonId = "5";
        assertThatThrownBy(() -> perform(
            post("/web/overtime/2")
                .param("id", "2")
                .param("person.id", otherPersonId)
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        )).hasCause(new AccessDeniedException("User '1' has not the correct permissions to edit overtime record of user '4'"));
    }

    private void mockSettings() {
        final Settings settings = new Settings();
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private void mockSettingsWithOvertimeReductionDisabled() {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeReductionWithoutApplicationActive(false);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
