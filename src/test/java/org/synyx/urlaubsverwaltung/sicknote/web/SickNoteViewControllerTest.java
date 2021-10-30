package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@ExtendWith(MockitoExtension.class)
class SickNoteViewControllerTest {

    private SickNoteViewController sut;

    private final int UNKNOWN_SICK_NOTE_ID = 0;
    private final int SOME_SICK_NOTE_ID = 15;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private SickNoteInteractionService sickNoteInteractionService;
    @Mock
    private SickNoteCommentService sickNoteCommentService;
    @Mock
    private SickNoteTypeService sickNoteTypeService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private PersonService personService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private SickNoteValidator sickNoteValidator;
    @Mock
    private SickNoteConvertFormValidator sickNoteConvertFormValidator;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {

        sut = new SickNoteViewController(sickNoteService,
            sickNoteInteractionService, sickNoteCommentService, sickNoteTypeService,
            vacationTypeService, personService, workDaysCountService, sickNoteValidator,
            sickNoteConvertFormValidator, settingsService, Clock.systemUTC());
    }

    @Test
    void ensureGetNewSickNoteProvidesCorrectModelAttributesAndView() throws Exception {

        when(personService.getActivePersons()).thenReturn(Collections.singletonList(somePerson()));
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(Collections.singletonList(someSickNoteType()));

        perform(get("/web/sicknote/new"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteForm.class)))
            .andExpect(model().attribute("persons", personService.getActivePersons()))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes()))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void ensureGetEditHasCorrectModelAttributes() throws Exception {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteForm.class)))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes()))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void ensureGetEditSickNoteForUnknownSickNoteIdThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/edit"))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensureGetEditSickNoteForInactiveThrowsSickNoteAlreadyInactiveException() {

        when(sickNoteService.getById(UNKNOWN_SICK_NOTE_ID)).thenReturn(Optional.of(someInactiveSickNote()));

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/edit"))
        ).hasCauseInstanceOf(SickNoteAlreadyInactiveException.class);
    }

    @Test
    void ensureGetSickNoteDetailsForUnknownSickNoteIdThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensureGetSickNoteDetailsAccessableForPersonWithRoleOfficeOrSickNoteOwner() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(status().isOk());

        final Person somePerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(somePerson);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNoteOfPerson(somePerson)));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(status().isOk());

        final Person officePerson = personWithRole(OFFICE);
        when(personService.getSignedInUser()).thenReturn(officePerson);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNoteOfPerson(officePerson)));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(status().isOk());
    }

    @Test
    void ensureGetSickNoteDetailsNotAccessableForOtherPersonIfNotRoleOffice() {

        final int somePersonId = 1;
        when(personService.getSignedInUser()).thenReturn(personWithId(somePersonId));

        final int anotherPersonId = 2;
        when(sickNoteService.getById(SOME_SICK_NOTE_ID))
            .thenReturn(Optional.of(sickNoteOfPerson(personWithId(anotherPersonId))));

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteDetailsProvidesCorrectModelAttributesAndView() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));
        when(sickNoteCommentService.getCommentsBySickNote(any(SickNote.class))).thenReturn(Collections.emptyList());

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(model().attribute("sickNote", instanceOf(ExtendedSickNote.class)))
            .andExpect(model().attribute("comment", instanceOf(SickNoteComment.class)))
            .andExpect(model().attribute("comments", instanceOf(List.class)))
            .andExpect(view().name("sicknote/sick_note"));
    }

    @Test
    void ensurePostNewSickNoteShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteValidator).validate(any(), any());

        perform(post("/web/sicknote"))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void ensurePostNewSickNoteCreatesSickNoteIfValidationSuccessful() throws Exception {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/sicknote/"));

        verify(sickNoteInteractionService).create(any(SickNote.class), eq(signedInPerson), any());
    }

    @Test
    void ensurePostNewSickNoteRedirectsToCreatedSickNote() throws Exception {

        when(personService.getSignedInUser()).thenReturn(somePerson());

        doAnswer(invocation -> {
            SickNote sickNote = invocation.getArgument(0);
            sickNote.setId(SOME_SICK_NOTE_ID);
            return sickNote;
        }).when(sickNoteInteractionService).create(any(SickNote.class), any(Person.class), any());

        perform(post("/web/sicknote/"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    void editPostSickNoteShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteValidator).validate(any(), any());

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void editPostSickNoteUpdatesSickNoteIfValidationSuccessful() throws Exception {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"));

        verify(sickNoteInteractionService).update(any(SickNote.class), eq(signedInPerson), any());
    }

    @Test
    void editPostSickNoteRedirectsToCreatedSickNote() throws Exception {

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    void ensurePostAddCommentThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->
            perform(post("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/comment"))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensurePostAddCommentAddsFlashAttributeAndRedirectsToSickNoteIfValidationFails() throws Exception {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("sickNote", "errors");
            return null;
        }).when(sickNoteValidator).validateComment(any(SickNoteComment.class), any(Errors.class));

        String ERRORS_ATTRIBUTE = "errors";
        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/comment"))
            .andExpect(flash().attribute(ERRORS_ATTRIBUTE, instanceOf(Errors.class)))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    void ensurePostAddCommentCreatesSickNoteCommentIfValidationSuccessful() throws Exception {

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/comment"));

        verify(sickNoteCommentService).create(any(SickNote.class), any(), eq(signedInPerson), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"comment", "convert", "cancel"})
    void ensureRedirectToSickNote(String path) throws Exception {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/" + path))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    void ensureGetConvertSickNoteToVacationForUnknownSickNoteIdThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/convert"))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensureGetConvertSickNoteToVacationThrowsSickNoteAlreadyInactiveException() {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someInactiveSickNote()));

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
        ).hasCauseInstanceOf(SickNoteAlreadyInactiveException.class);
    }

    @Test
    void ensureGetConvertSickNoteToVacationAddModel() throws Exception {

        overtimeActive(false);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        final List<VacationType> vacationTypes = List.of(new VacationType(1, true, HOLIDAY, "message_key"));
        when(vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME)).thenReturn(vacationTypes);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(model().attribute("sickNote", instanceOf(ExtendedSickNote.class)))
            .andExpect(model().attribute("sickNoteConvertForm", instanceOf(SickNoteConvertForm.class)))
            .andExpect(model().attribute("vacationTypes", vacationTypes));
    }

    @Test
    void ensureGetConvertSickNoteToVacationAddModelOvertimeTrue() throws Exception {

        overtimeActive(true);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        final List<VacationType> vacationTypes = List.of(new VacationType(1, true, HOLIDAY, "message_key"));
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(model().attribute("sickNote", instanceOf(ExtendedSickNote.class)))
            .andExpect(model().attribute("sickNoteConvertForm", instanceOf(SickNoteConvertForm.class)))
            .andExpect(model().attribute("vacationTypes", vacationTypes));
    }

    @Test
    void ensureGetConvertSickNoteToVacationUsesCorrectView() throws Exception {

        overtimeActive(false);

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(view().name("sicknote/sick_note_convert"));
    }

    @Test
    void ensurePostConvertSickNoteToVacationForUnknownSickNoteIdThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->
            perform(post("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/convert"))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensurePostConvertSickNoteToVacationFilledModelCorrectlyAndViewIfValidationFails() throws Exception {

        overtimeActive(false);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        final List<VacationType> vacationTypes = List.of(new VacationType(1, true, HOLIDAY, "message_key"));
        when(vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME)).thenReturn(vacationTypes);

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteConvertFormValidator).validate(any(), any());

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(view().name("sicknote/sick_note_convert"))
            .andExpect(model().attribute("errors", instanceOf(Errors.class)))
            .andExpect(model().attribute("sickNote", instanceOf(ExtendedSickNote.class)))
            .andExpect(model().attribute("sickNoteConvertForm", instanceOf(SickNoteConvertForm.class)))
            .andExpect(model().attribute("vacationTypes", vacationTypes));
    }

    @Test
    void ensurePostConvertSickNoteToVacationFilledModelCorrectlyAndViewIfValidationFailsWithOvertimeActive() throws Exception {

        overtimeActive(true);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        final List<VacationType> vacationTypes = List.of(new VacationType(1, true, HOLIDAY, "message_key"));
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteConvertFormValidator).validate(any(), any());

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(view().name("sicknote/sick_note_convert"))
            .andExpect(model().attribute("errors", instanceOf(Errors.class)))
            .andExpect(model().attribute("sickNote", instanceOf(ExtendedSickNote.class)))
            .andExpect(model().attribute("sickNoteConvertForm", instanceOf(SickNoteConvertForm.class)))
            .andExpect(model().attribute("vacationTypes", vacationTypes));
    }

    @Test
    void ensurePostConvertSickNoteToVacationConvertsSickNoteIfValidationSuccessful() throws Exception {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(someActiveSickNote()));

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"));

        verify(sickNoteInteractionService).convert(any(SickNote.class), any(Application.class), eq(signedInPerson));
    }

    @Test
    void ensureCancelSickNoteThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->
            perform(post("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/cancel"))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensureCancelSickNoteCancelsSickNoteCorrectly() throws Exception {

        final SickNote sickNote = someActiveSickNote();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));

        final Person signedInPerson = somePerson();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/cancel"));

        verify(sickNoteInteractionService).cancel(sickNote, signedInPerson);
    }

    private void overtimeActive(boolean active) {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(active);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private SickNote sickNoteOfPerson(Person somePerson) {
        final SickNote sickNote = new SickNote();
        sickNote.setPerson(somePerson);
        return sickNote;
    }

    private Person personWithRole(Role role) {
        final Person person = new Person();
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
