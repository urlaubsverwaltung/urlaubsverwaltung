package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import java.time.LocalDate;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import static org.synyx.urlaubsverwaltung.overtime.OvertimeAction.CREATED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@RunWith(MockitoJUnitRunner.class)
public class OvertimeViewControllerTest {

    private OvertimeViewController sut;

    @Mock
    private OvertimeService overtimeService;
    @Mock
    private PersonService personService;
    @Mock
    private OvertimeFormValidator validator;
    @Mock
    private DepartmentService departmentService;

    private Clock clock;

    @Before
    public void setUp() {
        clock = Clock.systemUTC();
        sut = new OvertimeViewController(overtimeService, personService, validator, departmentService, clock);
    }

    @Test
    public void postRecordOvertimeShowsFormIfValidationFails() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(Collections.singletonList(OFFICE));

        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/overtime"))
            .andExpect(model().attribute("overtime", instanceOf(OvertimeForm.class)))
            .andExpect(view().name("overtime/overtime_form"));

        verify(validator).validate(any(OvertimeForm.class), any(Errors.class));
    }

    @Test
    public void postUpdateOvertimeShowsFormIfValidationFails() throws Exception {

        final Overtime overtime = new Overtime(new Person(), LocalDate.MIN, LocalDate.MAX, BigDecimal.TEN);
        when(overtimeService.getOvertimeById(anyInt())).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(Collections.singletonList(OFFICE));

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
    public void showPersonalOvertime() throws Exception {

        final Person person = new Person();
        person.setId(5);
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overtime"));
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/overtime?person=5"));
    }

    @Test
    public void showOvertimeIsAllowed() throws Exception {

        final int year = Year.now(clock).getValue();

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, person)).thenReturn(true);

        final List<Overtime> records = List.of(new Overtime(person, LocalDate.MIN, LocalDate.MAX, BigDecimal.TEN));
        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, year)).thenReturn(records);

        when(overtimeService.getTotalOvertimeForPersonAndYear(person, year)).thenReturn(BigDecimal.ONE);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(BigDecimal.ZERO);

        final ResultActions resultActions = perform(get("/web/overtime").param("person", "5"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_list"));
        resultActions.andExpect(model().attribute("year", is(year)));
        resultActions.andExpect(model().attribute("person", is(person)));
        resultActions.andExpect(model().attribute("records", is(records)));
        resultActions.andExpect(model().attribute("overtimeTotal", is(BigDecimal.ONE)));
        resultActions.andExpect(model().attribute("overtimeLeft", is(BigDecimal.ZERO)));
    }

    @Test
    public void showOvertimeIsAllowedWithYear() throws Exception {

        final int year = 2012;

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, person)).thenReturn(true);

        final List<Overtime> records = List.of(new Overtime(person, LocalDate.MIN, LocalDate.MAX, BigDecimal.TEN));
        when(overtimeService.getOvertimeRecordsForPersonAndYear(person, year)).thenReturn(records);

        when(overtimeService.getTotalOvertimeForPersonAndYear(person, year)).thenReturn(BigDecimal.ONE);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(BigDecimal.ZERO);

        final ResultActions resultActions = perform(
            get("/web/overtime")
                .param("person", "5")
                .param("year", "2012")
        );
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_list"));
        resultActions.andExpect(model().attribute("year", is(year)));
        resultActions.andExpect(model().attribute("person", is(person)));
        resultActions.andExpect(model().attribute("records", is(records)));
        resultActions.andExpect(model().attribute("overtimeTotal", is(BigDecimal.ONE)));
        resultActions.andExpect(model().attribute("overtimeLeft", is(BigDecimal.ZERO)));
    }

    @Test(expected = NestedServletException.class)
    public void showOvertimeIsNotAllowed() throws Exception {

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, person)).thenReturn(false);

        perform(get("/web/overtime").param("person", "5"));
    }

    @Test
    public void showOvertimeDetails() throws Exception {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, BigDecimal.TEN);
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, overtimePerson)).thenReturn(true);

        final List<OvertimeComment> overtimeComments = List.of(new OvertimeComment(overtimePerson, overtime, CREATED));
        when(overtimeService.getCommentsForOvertime(overtime)).thenReturn(overtimeComments);

        when(overtimeService.getTotalOvertimeForPersonAndYear(overtimePerson, overtimeEndDate.getYear())).thenReturn(BigDecimal.ONE);
        when(overtimeService.getLeftOvertimeForPerson(overtimePerson)).thenReturn(BigDecimal.ZERO);

        final ResultActions resultActions = perform(get("/web/overtime/2"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_details"));
        resultActions.andExpect(model().attribute("record", is(overtime)));
        resultActions.andExpect(model().attribute("comments", is(overtimeComments)));
        resultActions.andExpect(model().attribute("overtimeTotal", is(BigDecimal.ONE)));
        resultActions.andExpect(model().attribute("overtimeLeft", is(BigDecimal.ZERO)));
    }

    @Test(expected = NestedServletException.class)
    public void showOvertimeDetailsIsNotAllowed() throws Exception {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, BigDecimal.TEN);
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(departmentService.isSignedInUserAllowedToAccessPersonData(signedInPerson, overtimePerson)).thenReturn(false);

        perform(get("/web/overtime/2"));
    }

    @Test
    public void recordOvertimeSignedInUserSame() throws Exception {

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
    }

    @Test
    public void recordOvertimePersonIdIsNull() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final ResultActions resultActions = perform(get("/web/overtime/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_form"));
        resultActions.andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))));
    }


    @Test(expected = NestedServletException.class)
    public void recordOvertimeSignedInUserIsNotSame() throws Exception {

        final int personId = 5;
        final Person person = new Person();
        person.setId(personId);
        when(personService.getPersonByID(personId)).thenReturn(Optional.of(person));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(get("/web/overtime/new").param("person", "5"));
    }

    @Test
    public void recordOvertimeSignedInUserIsNotSameButOffice() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final ResultActions resultActions = perform(get("/web/overtime/new"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_form"));
        resultActions.andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))));
    }

    @Test
    public void editOvertime() throws Exception {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, BigDecimal.TEN);
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));
        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final ResultActions resultActions = perform(get("/web/overtime/2/edit"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("overtime/overtime_form"));
        resultActions.andExpect(model().attribute("overtime", is(instanceOf(OvertimeForm.class))));
        resultActions.andExpect(model().attribute("person", is(overtimePerson)));
    }

    @Test(expected = NestedServletException.class)
    public void editOvertimeDifferentPersons() throws Exception {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, BigDecimal.TEN);
        overtime.setId(overtimeId);
        when(overtimeService.getOvertimeById(overtimeId)).thenReturn(Optional.of(overtime));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(get("/web/overtime/2/edit"));
    }

    @Test
    public void editOvertimeDifferentPersonsButOffice() throws Exception {

        final Person overtimePerson = new Person();

        final int overtimeId = 2;
        final LocalDate overtimeEndDate = LocalDate.MAX;
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, overtimeEndDate, BigDecimal.TEN);
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
    public void createOvertimeRecord() throws Exception {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, BigDecimal.TEN);
        overtime.setId(2);
        when(overtimeService.record(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);

        final ResultActions resultActions = perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("numberOfHours", "8")
                .param("comment", "To much work")
        );

        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/overtime/2"));
        resultActions.andExpect(flash().attribute("overtimeRecord", "CREATED"));
    }

    @Test(expected = NestedServletException.class)
    public void createOvertimeRecordNotSamePerson() throws Exception {

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);

        perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("numberOfHours", "8")
                .param("comment", "To much work")
        );
    }

    @Test
    public void createOvertimeRecordNotSamePersonButOffice() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, BigDecimal.TEN);
        overtime.setId(2);

        when(overtimeService.record(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);

        final ResultActions resultActions = perform(
            post("/web/overtime")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("numberOfHours", "8")
                .param("comment", "To much work")
        );

        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/overtime/2"));
        resultActions.andExpect(flash().attribute("overtimeRecord", "CREATED"));
    }

    @Test
    public void updateOvertime() throws Exception {

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
        when(personService.getSignedInUser()).thenReturn(overtimePerson);

        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, BigDecimal.TEN);
        overtime.setId(2);
        when(overtimeService.getOvertimeById(2)).thenReturn(Optional.of(overtime));

        when(overtimeService.record(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);

        final ResultActions resultActions = perform(
            post("/web/overtime/2")
                .param("id", "2")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("numberOfHours", "8")
                .param("comment", "To much work")
        );

        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/overtime/2"));
        resultActions.andExpect(flash().attribute("overtimeRecord", "EDITED"));
    }

    @Test(expected = NestedServletException.class)
    public void updateOvertimeIsNotSamePerson() throws Exception {

        when(personService.getSignedInUser()).thenReturn(new Person());

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, BigDecimal.TEN);
        overtime.setId(2);
        when(overtimeService.getOvertimeById(2)).thenReturn(Optional.of(overtime));

        perform(
            post("/web/overtime/2")
                .param("id", "2")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("numberOfHours", "8")
                .param("comment", "To much work")
        );
    }

    @Test
    public void updateOvertimeIsNotSamePersonButOffice() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        final Person overtimePerson = new Person();
        overtimePerson.setId(4);
        final Overtime overtime = new Overtime(overtimePerson, LocalDate.MIN, LocalDate.MAX, BigDecimal.TEN);
        overtime.setId(2);
        when(overtimeService.getOvertimeById(2)).thenReturn(Optional.of(overtime));

        when(overtimeService.record(any(Overtime.class), any(Optional.class), any(Person.class))).thenReturn(overtime);

        final ResultActions resultActions = perform(
            post("/web/overtime/2")
                .param("id", "2")
                .param("person.id", "4")
                .param("startDate", "02.07.2019")
                .param("endDate", "02.07.2019")
                .param("numberOfHours", "8")
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
