package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@RunWith(MockitoJUnitRunner.class)
public class SickNoteViewControllerTest {

    private SickNoteViewController sut;

    private final int UNKNOWN_SICK_NOTE_ID = 0;
    private final int SOME_SICK_NOTE_ID = 15;

    @Mock
    private SickNoteService sickNoteServiceMock;
    @Mock
    private SickNoteInteractionService sickNoteInteractionServiceMock;
    @Mock
    private SickNoteCommentService sickNoteCommentServiceMock;
    @Mock
    private SickNoteTypeService sickNoteTypeServiceMock;
    @Mock
    private VacationTypeService vacationTypeServiceMock;
    @Mock
    private PersonService personServiceMock;
    @Mock
    private WorkDaysService calendarServiceMock;
    @Mock
    private SickNoteValidator validatorMock;
    @Mock
    private SickNoteConvertFormValidator sickNoteConvertFormValidatorMock;

    @Before
    public void setUp() {

        sut = new SickNoteViewController(sickNoteServiceMock,
            sickNoteInteractionServiceMock, sickNoteCommentServiceMock, sickNoteTypeServiceMock,
            vacationTypeServiceMock, personServiceMock, calendarServiceMock, validatorMock,
            sickNoteConvertFormValidatorMock, Clock.systemUTC());
    }

    @Test
    public void ensureGetNewSickNoteProvidesCorrectModelAttributesAndView() throws Exception {

        when(personServiceMock.getActivePersons()).thenReturn(Collections.singletonList(somePerson()));
        when(sickNoteTypeServiceMock.getSickNoteTypes()).thenReturn(Collections.singletonList(someSickNoteType()));

        perform(get("/web/sicknote/new"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteForm.class)))
            .andExpect(model().attribute("persons", personServiceMock.getActivePersons()))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypeServiceMock.getSickNoteTypes()))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    public void ensureGetEditHasCorrectModelAttributes() throws Exception {

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteForm.class)))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypeServiceMock.getSickNoteTypes()))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    public void ensureGetEditSickNoteForUnknownSickNoteIdThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->

            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/edit"))

        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    public void ensureGetEditSickNoteForInactiveThrowsSickNoteAlreadyInactiveException() {

        when(sickNoteServiceMock.getById(UNKNOWN_SICK_NOTE_ID)).thenReturn(Optional.of(someInactiveSickNote()));

        assertThatThrownBy(() ->

            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/edit"))

        ).hasCauseInstanceOf(SickNoteAlreadyInactiveException.class);
    }

    @Test
    public void ensureGetSickNoteDetailsForUnknownSickNoteIdThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->

            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID))

        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    public void ensureGetSickNoteDetailsAccessableForPersonWithRoleOfficeOrSickNoteOwner() throws Exception {

        when(personServiceMock.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(status().isOk());

        final Person somePerson = somePerson();
        when(personServiceMock.getSignedInUser()).thenReturn(somePerson);
        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNoteOfPerson(somePerson)));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(status().isOk());

        final Person officePerson = personWithRole(OFFICE);
        when(personServiceMock.getSignedInUser()).thenReturn(officePerson);
        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNoteOfPerson(officePerson)));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(status().isOk());
    }

    @Test
    public void ensureGetSickNoteDetailsNotAccessableForOtherPersonIfNotRoleOffice() {

        final int somePersonId = 1;
        when(personServiceMock.getSignedInUser()).thenReturn(personWithId(somePersonId));

        final int anotherPersonId = 2;
        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID))
            .thenReturn(Optional.of(sickNoteOfPerson(personWithId(anotherPersonId))));

        assertThatThrownBy(() ->

            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void ensureGetSickNoteDetailsProvidesCorrectModelAttributesAndView() throws Exception {

        when(personServiceMock.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));
        when(sickNoteCommentServiceMock.getCommentsBySickNote(any(SickNote.class))).thenReturn(Collections.emptyList());

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(model().attribute("sickNote", instanceOf(ExtendedSickNote.class)))
            .andExpect(model().attribute("comment", instanceOf(SickNoteComment.class)))
            .andExpect(model().attribute("comments", instanceOf(List.class)))
            .andExpect(view().name("sicknote/sick_note"));
    }

    @Test
    public void ensurePostNewSickNoteShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {

            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(validatorMock).validate(any(), any());

        perform(post("/web/sicknote"))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    public void ensurePostNewSickNoteCreatesSickNoteIfValidationSuccessful() throws Exception {

        final Person signedInPerson = somePerson();
        when(personServiceMock.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/sicknote/"));

        verify(sickNoteInteractionServiceMock).create(any(SickNote.class), eq(signedInPerson), any());
    }

    @Test
    public void ensurePostNewSickNoteRedirectsToCreatedSickNote() throws Exception {

        when(personServiceMock.getSignedInUser()).thenReturn(somePerson());

        doAnswer(invocation -> {

            SickNote sickNote = invocation.getArgument(0);
            sickNote.setId(SOME_SICK_NOTE_ID);

            return sickNote;
        }).when(sickNoteInteractionServiceMock).create(any(SickNote.class), any(Person.class), any());

        perform(post("/web/sicknote/"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    public void editPostSickNoteShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {

            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(validatorMock).validate(any(), any());

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    public void editPostSickNoteUpdatesSickNoteIfValidationSuccessful() throws Exception {

        final Person signedInPerson = somePerson();
        when(personServiceMock.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"));

        verify(sickNoteInteractionServiceMock).update(any(SickNote.class), eq(signedInPerson), any());
    }

    @Test
    public void editPostSickNoteRedirectsToCreatedSickNote() throws Exception {

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    public void ensurePostAddCommentThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->

            perform(post("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/comment"))

        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    public void ensurePostAddCommentAddsFlashAttributeAndRedirectsToSickNoteIfValidationFails() throws Exception {

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        doAnswer(invocation -> {

            Errors errors = invocation.getArgument(1);
            errors.rejectValue("sickNote", "errors");
            return null;
        }).when(validatorMock).validateComment(any(SickNoteComment.class), any(Errors.class));

        String ERRORS_ATTRIBUTE = "errors";
        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/comment"))
            .andExpect(flash().attribute(ERRORS_ATTRIBUTE, instanceOf(Errors.class)))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    public void ensurePostAddCommentCreatesSickNoteCommentIfValidationSuccessful() throws Exception {

        final Person signedInPerson = somePerson();
        when(personServiceMock.getSignedInUser()).thenReturn(signedInPerson);

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/comment"));

        verify(sickNoteCommentServiceMock).create(any(SickNote.class), any(), eq(signedInPerson), any());
    }

    @Test
    public void ensurePostAddCommentRedirectsToSickNoteOfComment() throws Exception {

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/comment"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    public void ensureGetConvertSickNoteToVacationForUnknownSickNoteIdThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->

            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/convert"))

        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    public void ensureGetConvertSickNoteToVacationThrowsSickNoteAlreadyInactiveException() {

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someInactiveSickNote()));

        assertThatThrownBy(() ->

            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))

        ).hasCauseInstanceOf(SickNoteAlreadyInactiveException.class);
    }

    @Test
    public void ensureGetConvertSickNoteToVacationAddModel() throws Exception {

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));
        when(vacationTypeServiceMock.getVacationTypes()).thenReturn(Collections.emptyList());

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(model().attribute("sickNote", instanceOf(ExtendedSickNote.class)))
            .andExpect(model().attribute("sickNoteConvertForm", instanceOf(SickNoteConvertForm.class)))
            .andExpect(model().attribute("vacationTypes", instanceOf(List.class)));
    }

    @Test
    public void ensureGetConvertSickNoteToVacationUsesCorrectView() throws Exception {

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(view().name("sicknote/sick_note_convert"));
    }

    @Test
    public void ensurePostConvertSickNoteToVacationForUnknownSickNoteIdThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->

            perform(post("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/convert"))

        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    public void ensurePostConvertSickNoteToVacationShowsFormIfValidationFails() throws Exception {

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        doAnswer(invocation -> {

            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteConvertFormValidatorMock).validate(any(), any());

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(view().name("sicknote/sick_note_convert"));
    }

    @Test
    public void ensurePostConvertSickNoteToVacationConvertsSickNoteIfValidationSuccessful() throws Exception {

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        final Person signedInPerson = somePerson();
        when(personServiceMock.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"));

        verify(sickNoteInteractionServiceMock).convert(any(SickNote.class), any(Application.class), eq(signedInPerson));
    }

    @Test
    public void ensurePostConvertSickNoteToVacationRedirectsToSickNoteOfConvert() throws Exception {

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    public void ensureCancelSickNoteThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->

            perform(post("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/cancel"))

        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    public void ensureCancelSickNoteCancelsSickNoteCorrectly() throws Exception {

        final SickNote sickNote = someActiveSickNote();
        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));

        final Person signedInPerson = somePerson();
        when(personServiceMock.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/cancel"));

        verify(sickNoteInteractionServiceMock).cancel(sickNote, signedInPerson);
    }

    @Test
    public void ensureCancelSickNoteRedirectsToCanceledSickNote() throws Exception {

        when(sickNoteServiceMock.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/cancel"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    private SickNote sickNoteOfPerson(Person somePerson) {

        SickNote sickNote = new SickNote();
        sickNote.setPerson(somePerson);
        return sickNote;
    }

    private Person personWithRole(Role role) {

        Person person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(role));
        return person;
    }

    private SickNote someInactiveSickNote() {

        SickNote sickNote = new SickNote();
        sickNote.setStatus(SickNoteStatus.CANCELLED);
        return sickNote;
    }

    private SickNote someActiveSickNote() {

        SickNote sickNote = new SickNote();
        sickNote.setStatus(SickNoteStatus.ACTIVE);
        return sickNote;
    }

    private Person somePerson() {

        return new Person();
    }

    private Person personWithId(int personId) {

        Person person = new Person();
        person.setId(personId);

        return person;
    }

    private SickNoteType someSickNoteType() {

        return new SickNoteType();
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(sut).build().perform(builder);
    }
}
