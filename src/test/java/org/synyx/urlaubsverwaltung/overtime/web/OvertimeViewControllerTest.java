package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.springframework.web.util.NestedServletException;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new OvertimeViewController(overtimeService, personService, validator, departmentService, clock);
    }

    @Test
    void postRecordOvertimeShowsFormIfValidationFails() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));

        final Person overtimePerson = new Person();

        when(personService.getSignedInUser()).thenReturn(signedInPerson);
        when(personService.getPersonByID(1337)).thenReturn(Optional.of(overtimePerson));

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

        final Overtime overtime = new Overtime(new Person(), LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        when(overtimeService.getOvertimeById(anyInt())).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/overtime/5"))
            .andExpect(model().attribute("overtime", instanceOf(OvertimeForm.class)))
            .andExpect(view().name("overtime/overtime_form"));
    }

    @Test
    void showPersonalOvertime() throws Exception {

        final Person person = new Person();
        person.setId(5);
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overtime"));
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/overtime?person=5"));
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

        final List<Overtime> records = List.of(new Overtime(person, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10)));
        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, year)).thenReturn(records);

        when(overtimeService.getTotalOvertimeForPersonAndYear(person, year)).thenReturn(Duration.ofHours(1));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ZERO);

        final ResultActions resultActions = perform(get("/web/overtime").param("person", "5"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_list"));
        resultActions.andExpect(model().attribute("year", is(year)));
        resultActions.andExpect(model().attribute("person", is(person)));
        resultActions.andExpect(model().attribute("signedInUser", is(signedInPerson)));
        resultActions.andExpect(model().attribute("records", is(records)));
        resultActions.andExpect(model().attribute("overtimeTotal", is(BigDecimal.ONE)));
        resultActions.andExpect(model().attribute("overtimeLeft", is(BigDecimal.ZERO)));
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

        final List<Overtime> records = List.of(new Overtime(person, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10)));
        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, year)).thenReturn(records);

        when(overtimeService.getTotalOvertimeForPersonAndYear(person, year)).thenReturn(Duration.ofHours(1));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ZERO);

        final ResultActions resultActions = perform(
            get("/web/overtime")
                .param("person", "5")
                .param("year", "2012")
        );
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_list"));
        resultActions.andExpect(model().attribute("year", is(year)));
        resultActions.andExpect(model().attribute("person", is(person)));
        resultActions.andExpect(model().attribute("signedInUser", is(signedInPerson)));
        resultActions.andExpect(model().attribute("records", is(records)));
        resultActions.andExpect(model().attribute("overtimeTotal", is(BigDecimal.ONE)));
        resultActions.andExpect(model().attribute("overtimeLeft", is(BigDecimal.ZERO)));
    }

    @Test
    void showOvertimeIsNotAllowed() {

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, person)).thenReturn(false);

        assertThatThrownBy(() -> perform(get("/web/overtime").param("person", "5")))
            .isInstanceOf(NestedServletException.class);
    }

    @Test
    void showOvertimeDetails() throws Exception {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, Duration.ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, overtimePerson)).thenReturn(true);

        final List<OvertimeComment> overtimeComments = List.of(new OvertimeComment(overtimePerson, overtime, CREATED, Clock.systemUTC()));
        when(overtimeService.getCommentsForOvertime(overtime)).thenReturn(overtimeComments);

        when(overtimeService.getTotalOvertimeForPersonAndYear(overtimePerson, overtimeEndDate.getYear())).thenReturn(Duration.ofHours(1));
        when(overtimeService.getLeftOvertimeForPerson(overtimePerson)).thenReturn(Duration.ZERO);

        final ResultActions resultActions = perform(get("/web/overtime/2"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_details"));
        resultActions.andExpect(model().attribute("record", is(overtime)));
        resultActions.andExpect(model().attribute("comments", is(overtimeComments)));
        resultActions.andExpect(model().attribute("signedInUser", is(signedInPerson)));
        resultActions.andExpect(model().attribute("overtimeTotal", is(BigDecimal.ONE)));
        resultActions.andExpect(model().attribute("overtimeLeft", is(BigDecimal.ZERO)));
    }

    @Test
    void showOvertimeDetailsIsNotAllowed() {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, overtimePerson)).thenReturn(false);

        assertThatThrownBy(() -> perform(get("/web/overtime/2")))
            .isInstanceOf(NestedServletException.class);
    }

    @Test
    void recordOvertimeSignedInUserSame() throws Exception {

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overtime/new").param("person", "5"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_form"));
        resultActions.andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))));
        resultActions.andExpect(model().attribute("person", is(person)));
        resultActions.andExpect(model().attribute("signedInUser", is(person)));
    }

    @Test
    void recordOvertimePersonIdIsNull() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overtime/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_form"));
        resultActions.andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))));
    }

    @Test
    void recordOvertimeSignedInUserIsNotSame() {

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        assertThatThrownBy(() -> perform(get("/web/overtime/new").param("person", "5")))
            .isInstanceOf(NestedServletException.class);
    }

    @Test
    void recordOvertimeSignedInUserIsNotSameButOffice() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final ResultActions resultActions = perform(get("/web/overtime/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_form"));
        resultActions.andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))));
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

        final ResultActions resultActions = perform(get("/web/overtime/2/edit"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_form"));
        resultActions.andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))));
        resultActions.andExpect(model().attribute("person", is(overtimePerson)));
        resultActions.andExpect(model().attribute("signedInUser", is(overtimePerson)));
    }

    @Test
    void editOvertimeDifferentPersons() {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, Duration.ofHours(10));
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        assertThatThrownBy(() -> perform(get("/web/overtime/2/edit")))
            .isInstanceOf(NestedServletException.class);
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

        final ResultActions resultActions = perform(get("/web/overtime/2/edit"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_form"));
    }

    @Test
    void createOvertimeRecord() throws Exception {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(2);
        when(overtimeService.record(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);

        final ResultActions resultActions = perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        );

        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/overtime/2"));
        resultActions.andExpect(flash().attribute("overtimeRecord", "CREATED"));
    }

    @Test
    void ensureOvertimeHoursMustBeGreaterZero() throws Exception {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);

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
        )).isInstanceOf(NestedServletException.class);
    }

    @Test
    void createOvertimeRecordNotSamePersonButOffice() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, Duration.ofHours(10));
        overtime.setId(2);

        when(overtimeService.record(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);

        final ResultActions resultActions = perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        );

        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/overtime/2"));
        resultActions.andExpect(flash().attribute("overtimeRecord", "CREATED"));
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

        final ResultActions resultActions = perform(
            post("/web/overtime/2")
                .param("id", "2")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        );

        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/overtime/2"));
        resultActions.andExpect(flash().attribute("overtimeRecord", "EDITED"));
    }

    @Test
    void updateOvertimeIsNotSamePerson() {

        when(personService.getSignedInUser()).thenReturn(new Person());

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
        )).isInstanceOf(NestedServletException.class);
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

        final ResultActions resultActions = perform(
            post("/web/overtime/2")
                .param("id", "2")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("hours", "8")
                .param("comment", "To much work")
        );

        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/overtime/2"));
        resultActions.andExpect(flash().attribute("overtimeRecord", "EDITED"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
