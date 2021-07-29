package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.overtime.settings.OvertimeSettingsEntity;
import org.synyx.urlaubsverwaltung.overtime.settings.OvertimeSettingsService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.CREATED;
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
    private OvertimeSettingsService settingsService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new OvertimeViewController(overtimeService, personService, validator, departmentService, settingsService, clock);
    }

    @Test
    void postRecordOvertimeShowsFormIfValidationFails() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));

        final Person overtimePerson = new Person();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getPersonByID(1337)).thenReturn(Optional.of(overtimePerson));
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
            .andExpect(model().attribute("signedInUser", signedInPerson))
            .andExpect(view().name("overtime/overtime_form"));

        verify(validator).validate(any(OvertimeForm.class), any(Errors.class));
    }

    @Test
    void postUpdateOvertimeShowsFormIfValidationFails() throws Exception {

        Person person = new Person();
        person.setId(1);
        final Overtime overtime = new Overtime(person, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        when(overtimeService.getOvertimeById(anyInt())).thenReturn(Optional.of(overtime));

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
        person.setId(5);
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overtime"));
        resultActions
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/overtime?person=5"));
    }

    @Test
    void showUsersOvertimeListAndIsAllowedToAddOvertime() throws Exception {

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overtime").param("person", "5"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("userIsAllowedToWriteOvertime", is(true)));
    }

    @Test
    void showUsersOvertimeListAndIsNotAllowedToAddOvertime() throws Exception {

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));
        when(departmentService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(false);
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overtime").param("person", "5"));

        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("userIsAllowedToWriteOvertime", is(false)));
    }

    @Test
    void showOvertimeIsAllowed() throws Exception {

        final int year = Year.now(clock).getValue();

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, person)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, person)).thenReturn(true);

        final Overtime overtime = new Overtime(person, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        final List<Overtime> records = List.of(overtime);
        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, year)).thenReturn(records);

        when(overtimeService.getTotalOvertimeForPersonAndYear(person, year)).thenReturn(Duration.ofHours(1));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ZERO);

        final List<OvertimeListRecordDto> recordDtos = List.of(
            new OvertimeListRecordDto(overtime.getId(), overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getLastModificationDate()));

        final ResultActions resultActions = perform(get("/web/overtime").param("person", "5"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_list"))
            .andExpect(model().attribute("year", is(year)))
            .andExpect(model().attribute("person", is(person)))
            .andExpect(model().attribute("signedInUser", is(signedInPerson)))
            .andExpect(model().attribute("overtimeTotal", is(Duration.ofHours(1))))
            .andExpect(model().attribute("overtimeLeft", is(Duration.ZERO)))
            .andExpect(model().attribute("userIsAllowedToWriteOvertime", is(true)));

        assertThat(resultActions.andReturn().getModelAndView().getModel().get("records")).usingRecursiveComparison().isEqualTo(recordDtos);
    }

    @Test
    void showOvertimeIsAllowedWithYear() throws Exception {

        final int year = 2012;

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, person)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, person)).thenReturn(true);

        final Overtime overtime = new Overtime(person, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        final List<Overtime> records = List.of(overtime);
        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, year)).thenReturn(records);

        when(overtimeService.getTotalOvertimeForPersonAndYear(person, year)).thenReturn(Duration.ofHours(1));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ZERO);

        final List<OvertimeListRecordDto> recordDtos = List.of(
            new OvertimeListRecordDto(overtime.getId(), overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getLastModificationDate()));

        final ResultActions resultActions = perform(
            get("/web/overtime")
                .param("person", "5")
                .param("year", "2012")
        );

        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_list"))
            .andExpect(model().attribute("year", is(year)))
            .andExpect(model().attribute("person", is(person)))
            .andExpect(model().attribute("signedInUser", is(signedInPerson)))
            .andExpect(model().attribute("overtimeTotal", is(Duration.ofHours(1))))
            .andExpect(model().attribute("overtimeLeft", is(Duration.ZERO)));

        assertThat(resultActions.andReturn().getModelAndView().getModel().get("records")).usingRecursiveComparison().isEqualTo(recordDtos);
    }

    @Test
    void showOvertimeIsNotAllowed() {

        final Person person = new Person();
        person.setId(5);
        when(personService.getPersonByID(5)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, person)).thenReturn(false);

        assertThatThrownBy(() -> perform(get("/web/overtime").param("person", "5")))
            .hasCause(new AccessDeniedException("User '1' has not the correct permissions to see overtime records of user '5'"));
    }

    @Test
    void showOvertimeDetails() throws Exception {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, Duration.ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(overtimePerson, overtimePerson)).thenReturn(true);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(true);

        final OvertimeComment comment = new OvertimeComment(overtimePerson, overtime, CREATED, Clock.systemUTC());
        final List<OvertimeComment> overtimeComments = List.of(comment);
        when(overtimeService.getCommentsForOvertime(overtime)).thenReturn(overtimeComments);

        when(overtimeService.getTotalOvertimeForPersonAndYear(overtimePerson, overtimeEndDate.getYear())).thenReturn(Duration.ofHours(1));
        when(overtimeService.getLeftOvertimeForPerson(overtimePerson)).thenReturn(Duration.ZERO);

        final OvertimeDetailPersonDto personDto = new OvertimeDetailPersonDto(overtimePerson.getId(), overtimePerson.getEmail(), overtimePerson.getNiceName(), overtimePerson.getGravatarURL());
        final OvertimeDetailRecordDto record = new OvertimeDetailRecordDto(overtimeId, personDto, overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getLastModificationDate());
        final List<OvertimeCommentDto> commentDtos = List.of(new OvertimeCommentDto(new OvertimeCommentPersonDto(comment.getPerson().getNiceName(), comment.getPerson().getGravatarURL()), comment.getAction().toString(), comment.getDate(), comment.getText()));

        final ResultActions resultActions = perform(get("/web/overtime/2"));
        assertThat(resultActions.andReturn().getModelAndView().getModel().get("record")).usingRecursiveComparison().isEqualTo(record);
        assertThat(resultActions.andReturn().getModelAndView().getModel().get("comments")).usingRecursiveComparison().isEqualTo(commentDtos);

        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_details"))
            .andExpect(model().attribute("signedInUser", is(overtimePerson)))
            .andExpect(model().attribute("overtimeTotal", is(Duration.ofHours(1))))
            .andExpect(model().attribute("overtimeLeft", is(Duration.ZERO)))
            .andExpect(model().attribute("userIsAllowedToWriteOvertime", is(true)));
    }

    @Test
    void showOvertimeDetailsIsNotAllowed() {

        final Person overtimePerson = new Person();
        overtimePerson.setId(5);

        final int overtimeId = 2;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, overtimePerson)).thenReturn(false);

        assertThatThrownBy(() -> perform(get("/web/overtime/2")))
            .hasCause(new AccessDeniedException("User '1' has not the correct permissions to see overtime records of user '5'"));
    }

    @Test
    void recordOvertimeSignedInUserSame() throws Exception {

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);

        mockSettings();

        final ResultActions resultActions = perform(get("/web/overtime/new").param("person", "5"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_form"))
            .andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))))
            .andExpect(model().attribute("person", is(person)))
            .andExpect(model().attribute("signedInUser", is(person)));
    }

    @Test
    void recordOvertimeSignedInUserSameButOnlyPrivilegedAreAllowed() {

        final Person person = new Person();
        person.setId(5);
        person.setPermissions(List.of(USER));

        when(personService.getPersonByID(5)).thenReturn(Optional.of(person));
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
        person.setId(5);
        when(personService.getPersonByID(5)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        assertThatThrownBy(() -> perform(get("/web/overtime/new").param("person", "5")))
            .hasCause(new AccessDeniedException("User '1' has not the correct permissions to record overtime for user '5'"));
    }

    @Test
    void recordOvertimeSignedInUserIsNotSameButOffice() throws Exception {

        final Person overtimePerson = new Person();
        final int overtimePersonId = 1;
        overtimePerson.setId(overtimePersonId);

        final Person signedInPerson = new Person();
        signedInPerson.setId(2);
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
    void editOvertime() throws Exception {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, Duration.ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));
        when(personService.getSignedInUser()).thenReturn(overtimePerson);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(true);

        mockSettings();

        final ResultActions resultActions = perform(get("/web/overtime/2/edit"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_form"))
            .andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))))
            .andExpect(model().attribute("person", is(overtimePerson)))
            .andExpect(model().attribute("signedInUser", is(overtimePerson)));
    }

    @Test
    void editOvertimeSignedInUserSameButOnlyPrivilegedAreAllowed() {

        final Person overtimePerson = new Person();
        overtimePerson.setId(5);
        overtimePerson.setPermissions(List.of(USER));

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, Duration.ofHours(10));
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
        overtimePerson.setId(5);

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, Duration.ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        signedInPerson.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        assertThatThrownBy(() -> perform(get("/web/overtime/2/edit")))
            .hasCause(new AccessDeniedException("User '1' has not the correct permissions to edit overtime record of user '5'"));
    }

    @Test
    void editOvertimeDifferentPersonsButOffice() throws Exception {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, Duration.ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(overtimeService.isUserIsAllowedToWriteOvertime(signedInPerson, overtimePerson)).thenReturn(true);

        mockSettings();

        final ResultActions resultActions = perform(get("/web/overtime/2/edit"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(view().name("overtime/overtime_form"));
    }

    @Test
    void ensureNewOvertimeDoesNotShowReductionWhenFeatureIsDisabled() throws Exception {

        final Person person = new Person();
        person.setId(1);
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
        overtimePerson.setId(4);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(2);
        when(overtimeService.record(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);
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

    @Test
    void ensureCreateOvertimeValidationErrorPageDoesNotShowReductionWhenFeatureIsDisabled() throws Exception {

        final Person person = new Person();
        person.setId(1);
        when(personService.getSignedInUser()).thenReturn(person);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);

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
            .andExpect(model().attribute("overtimeReductionPossible", is(false)));
    }

    @Test
    void createOvertimeRecordButOnlyPrivilegedAreAllowed() {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
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
        overtimePerson.setId(4);
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
        overtimePerson.setId(4);
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
        signedInPerson.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);

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
        overtimePerson.setId(4);

        final Overtime overtime = new Overtime(signedInPerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(2);
        when(overtimeService.record(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);
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

    @Test
    void updateOvertime() throws Exception {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(2);
        when(overtimeService.getOvertimeById(2)).thenReturn(Optional.of(overtime));
        when(overtimeService.record(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);
        when(overtimeService.isUserIsAllowedToWriteOvertime(overtimePerson, overtimePerson)).thenReturn(true);

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
    void ensureUpdateOvertimePageDoesNotShowReductionWhenFeatureIsDisabled() throws Exception {

        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);

        final Overtime overtime = new Overtime(person, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(8));
        overtime.setId(2);
        when(overtimeService.getOvertimeById(2)).thenReturn(Optional.of(overtime));

        mockSettingsWithOvertimeReductionDisabled();

        final ResultActions resultActions = perform(get("/web/overtime/2/edit"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("overtimeReductionPossible", is(false)));
    }

    @Test
    void ensureUpdateOvertimeValidationErrorPageDoesNotShowReductionWhenFeatureIsDisabled() throws Exception {

        final Person person = new Person();
        person.setId(1);
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final Overtime overtime = new Overtime(person, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(8));
        overtime.setId(2);
        when(overtimeService.getOvertimeById(2)).thenReturn(Optional.of(overtime));
        when(overtimeService.isUserIsAllowedToWriteOvertime(person, person)).thenReturn(true);

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
            .andExpect(model().attribute("overtimeReductionPossible", is(false)));
    }

    @Test
    void updateOvertimeRecordButOnlyPrivilegedAreAllowed() {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
        overtimePerson.setPermissions(List.of(USER));

        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(2);
        when(overtimeService.getOvertimeById(2)).thenReturn(Optional.of(overtime));
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
        signedInPerson.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(2);
        when(overtimeService.getOvertimeById(2)).thenReturn(Optional.of(overtime));

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
        overtimePerson.setId(4);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(2);
        when(overtimeService.getOvertimeById(2)).thenReturn(Optional.of(overtime));
        when(overtimeService.record(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);
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
        signedInPerson.setId(1);
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(2);
        when(overtimeService.getOvertimeById(2)).thenReturn(Optional.of(overtime));

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
        final OvertimeSettingsEntity settings = new OvertimeSettingsEntity();
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private void mockSettingsWithOvertimeReductionDisabled() {
        OvertimeSettingsEntity settings = new OvertimeSettingsEntity();
        settings.setOvertimeReductionWithoutApplicationActive(false);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
