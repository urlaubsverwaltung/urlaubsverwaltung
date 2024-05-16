package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentFormDto;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentFormValidator;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
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
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_CANCEL;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_COMMENT;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.CANCELLED;

@ExtendWith(MockitoExtension.class)
class SickNoteViewControllerTest {

    private SickNoteViewController sut;

    private final long UNKNOWN_SICK_NOTE_ID = 0;
    private final long SOME_SICK_NOTE_ID = 15;

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
    private VacationTypeViewModelService vacationTypeViewModelService;
    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private SickNoteValidator sickNoteValidator;

    @Mock
    private SickNoteCommentFormValidator sickNoteCommentFormValidator;
    @Mock
    private SickNoteConvertFormValidator sickNoteConvertFormValidator;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new SickNoteViewController(sickNoteService,
            sickNoteInteractionService, sickNoteCommentService, sickNoteTypeService,
            vacationTypeService, vacationTypeViewModelService, personService, departmentService, sickNoteValidator,
            sickNoteCommentFormValidator, sickNoteConvertFormValidator, settingsService, Clock.systemUTC());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void ensureGetNewSickNoteProvidesCorrectModelAttributesAndViewForRole(Role role) throws Exception {

        final Person personWithRole = personWithRole(role, SICK_NOTE_ADD);
        personWithRole.setId(1L);
        when(personService.getSignedInUser()).thenReturn(personWithRole);

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(personWithRole));

        final List<Person> activePersons = of(new Person());
        when(personService.getActivePersons()).thenReturn(activePersons);
        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/new").param("person", "1"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteFormDto.class)))
            .andExpect(model().attribute("persons", activePersons))
            .andExpect(model().attribute("person", personWithRole))
            .andExpect(model().attribute("signedInUser", personWithRole))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypes))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void ensureGetNewSickNoteProvidesCorrectModelAttributesAndViewWithOtherPersonForRole(Role role) throws Exception {

        final Person personWithRole = personWithRole(role, SICK_NOTE_ADD);
        when(personService.getSignedInUser()).thenReturn(personWithRole);

        final Person otherPerson = personWithId(42);
        when(personService.getPersonByID(42L)).thenReturn(Optional.of(otherPerson));

        final List<Person> activePersons = of(new Person());
        when(personService.getActivePersons()).thenReturn(activePersons);
        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/new").param("person", "42"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteFormDto.class)))
            .andExpect(model().attribute("person", otherPerson))
            .andExpect(model().attribute("persons", activePersons))
            .andExpect(model().attribute("signedInUser", personWithRole))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypes))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void ensureGetNewSickNoteProvidesCorrectModelAttributesAndViewForDepartmentHead() throws Exception {

        final Person departmentHead = personWithRole(DEPARTMENT_HEAD, SICK_NOTE_ADD);
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final List<Person> departmentPersons = of(new Person());
        when(departmentService.getManagedMembersOfDepartmentHead(departmentHead)).thenReturn(departmentPersons);
        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/new"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteFormDto.class)))
            .andExpect(model().attribute("persons", departmentPersons))
            .andExpect(model().attribute("signedInUser", departmentHead))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypes))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void ensureGetNewSickNoteProvidesCorrectModelAttributesAndViewForSecondStageAuthority() throws Exception {

        final Person secondStageAuthority = personWithRole(SECOND_STAGE_AUTHORITY, SICK_NOTE_ADD);
        when(personService.getSignedInUser()).thenReturn(secondStageAuthority);

        final List<Person> departmentPersons = of(new Person());
        when(departmentService.getManagedMembersForSecondStageAuthority(secondStageAuthority)).thenReturn(departmentPersons);
        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/new"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteFormDto.class)))
            .andExpect(model().attribute("persons", departmentPersons))
            .andExpect(model().attribute("signedInUser", secondStageAuthority))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypes))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void ensureGetNewSickNoteProvidesCorrectModelAttributesAndViewForDepartmentHeadAndSecondStageAuthority() throws Exception {

        final Person departmentHeadAndSsa = new Person();
        departmentHeadAndSsa.setId(1L);
        departmentHeadAndSsa.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSsa);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("firstname");
        person.setLastName("lastname");
        when(departmentService.getManagedMembersOfDepartmentHead(departmentHeadAndSsa)).thenReturn(of(person));

        final Person person2 = new Person();
        person2.setId(3L);
        person2.setFirstName("firstname two");
        person2.setLastName("lastname two");
        when(departmentService.getManagedMembersForSecondStageAuthority(departmentHeadAndSsa)).thenReturn(of(person2));

        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/new"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteFormDto.class)))
            .andExpect(model().attribute("persons", of(person, person2)))
            .andExpect(model().attribute("signedInUser", departmentHeadAndSsa))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypes))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void ensureGetNewSickNoteManagedMembersDistinct() throws Exception {

        final Person departmentHeadAndSsa = new Person();
        departmentHeadAndSsa.setId(1L);
        departmentHeadAndSsa.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSsa);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("firstname");
        person.setLastName("lastname");
        when(departmentService.getManagedMembersOfDepartmentHead(departmentHeadAndSsa)).thenReturn(of(person));
        when(departmentService.getManagedMembersForSecondStageAuthority(departmentHeadAndSsa)).thenReturn(of(person));

        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/new"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteFormDto.class)))
            .andExpect(model().attribute("persons", of(person)))
            .andExpect(model().attribute("signedInUser", departmentHeadAndSsa))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypes))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void ensureGetNewSickNoteManagedMembersIsOrdered() throws Exception {

        final Person departmentHeadAndSsa = new Person();
        departmentHeadAndSsa.setId(1L);
        departmentHeadAndSsa.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSsa);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("B");
        person.setLastName("B");
        when(departmentService.getManagedMembersOfDepartmentHead(departmentHeadAndSsa)).thenReturn(of(person));

        final Person person2 = new Person();
        person2.setId(3L);
        person2.setFirstName("A");
        person2.setLastName("A");
        when(departmentService.getManagedMembersForSecondStageAuthority(departmentHeadAndSsa)).thenReturn(of(person2));

        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/new"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteFormDto.class)))
            .andExpect(model().attribute("persons", of(person2, person)))
            .andExpect(model().attribute("signedInUser", departmentHeadAndSsa))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypes))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void ensureGetEditHasCorrectModelAttributes() throws Exception {

        final Person office = personWithRole(USER, OFFICE);
        office.setId(1L);
        when(personService.getSignedInUser()).thenReturn(office);

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("sickNote", instanceOf(SickNoteFormDto.class)))
            .andExpect(model().attribute("sickNoteTypes", sickNoteTypes))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void ensureGetEditIsAccessibleForOffice() throws Exception {

        final Person office = personWithRole(USER, OFFICE);
        office.setId(1L);
        when(personService.getSignedInUser()).thenReturn(office);

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(status().isOk());
    }

    @Test
    void ensureGetEditIsAccessibleForDepartmentHead() throws Exception {

        final Person departmentHead = personWithRole(USER, DEPARTMENT_HEAD, SICK_NOTE_EDIT);
        departmentHead.setId(1L);
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final SickNote sickNote = SickNote.builder().person(new Person()).status(ACTIVE).build();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, sickNote.getPerson())).thenReturn(true);

        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(status().isOk());
    }

    @Test
    void ensureGetEditIsAccessibleForSecondStageAuthority() throws Exception {

        final Person ssa = personWithRole(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_EDIT);
        ssa.setId(1L);
        when(personService.getSignedInUser()).thenReturn(ssa);

        final SickNote sickNote = SickNote.builder().person(new Person()).status(ACTIVE).build();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, sickNote.getPerson())).thenReturn(true);

        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(status().isOk());
    }

    @Test
    void ensureGetEditIsAccessibleForBoss() throws Exception {

        final Person departmentHead = personWithRole(USER, BOSS, SICK_NOTE_EDIT);
        departmentHead.setId(1L);
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final SickNote sickNote = SickNote.builder().person(new Person()).status(ACTIVE).build();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));

        final List<SickNoteType> sickNoteTypes = of(someSickNoteType());
        when(sickNoteTypeService.getSickNoteTypes()).thenReturn(sickNoteTypes);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(status().isOk());
    }

    @Test
    void ensureGetEditSickNoteForUnknownSickNoteIdThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/edit"))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensureGetEditSickNoteForInactiveThrowsSickNoteAlreadyInactiveException() {

        when(sickNoteService.getById(UNKNOWN_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(CANCELLED).build()));

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/edit"))
        ).hasCauseInstanceOf(SickNoteAlreadyInactiveException.class);
    }

    @Test
    void ensureGetSickNoteEditIsNotAccessibleForPerson() {

        final Person signedInUser = personWithRole(USER);
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = personWithRole(USER);
        person.setId(2L);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(false);

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteEditIsNotAccessibleForPersonWithDepartmentHeadWithoutEditRole() {

        final Person departmentHeadPerson = personWithRole(USER, DEPARTMENT_HEAD);
        departmentHeadPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(departmentHeadPerson);

        final Person person = personWithRole(USER);
        person.setId(2L);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHeadPerson, person)).thenReturn(true);

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteEditIsNotAccessibleForPersonWithDepartmentHeadForWrongUser() {

        final Person departmentHeadPerson = personWithRole(USER, DEPARTMENT_HEAD, SICK_NOTE_EDIT);
        departmentHeadPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(departmentHeadPerson);

        final Person person = personWithRole(USER);
        person.setId(2L);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHeadPerson, person)).thenReturn(false);

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteEditIsNotAccessibleForPersonWithSecondStageAuthorityForWrongUser() {

        final Person departmentHeadPerson = personWithRole(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_EDIT);
        departmentHeadPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(departmentHeadPerson);

        final Person person = personWithRole(USER);
        person.setId(2L);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHeadPerson, person)).thenReturn(false);

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteEditIsNotAccessibleForPersonWithSecondStageAuthorityWithoutEditRole() {

        final Person departmentHeadPerson = personWithRole(USER, SECOND_STAGE_AUTHORITY);
        departmentHeadPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(departmentHeadPerson);

        final Person person = personWithRole(USER);
        person.setId(2L);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHeadPerson, person)).thenReturn(true);

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteDetailsForUnknownSickNoteIdThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensureGetSickNoteDetailsAccessibleForSickNoteOwner() throws Exception {

        final Person somePerson = new Person();
        when(personService.getSignedInUser()).thenReturn(somePerson);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(somePerson).build()));
        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID)).andExpect(status().isOk());
    }

    @Test
    void ensureGetSickNoteDetailsAccessibleForPersonWithRoleOffice() throws Exception {

        final Person officePerson = personWithRole(OFFICE);
        when(personService.getSignedInUser()).thenReturn(officePerson);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).build()));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID)).andExpect(status().isOk());
    }

    @Test
    void ensureCannotGetSickNoteDetailsAccessibleForPersonWithRoleBoss() {

        final Person bossPerson = personWithRole(BOSS);
        when(personService.getSignedInUser()).thenReturn(bossPerson);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).build()));

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteDetailsAccessibleForPersonWithRoleBossAndSickNoteView() throws Exception {

        final Person boss = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW));
        boss.setId(1L);
        when(personService.getSignedInUser()).thenReturn(boss);

        final Person person = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        person.setId(2L);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID)).andExpect(status().isOk());
    }

    @Test
    void ensureGetSickNoteDetailsAccessibleForPersonWithRoleDepartmentHeadAndSickNoteView() throws Exception {

        final Person departmentHeadPerson = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        departmentHeadPerson.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        departmentHeadPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(departmentHeadPerson);

        final Person person = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        person.setId(2L);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHeadPerson, person)).thenReturn(true);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID)).andExpect(status().isOk());
    }

    @Test
    void ensureGetSickNoteDetailsAccessibleForPersonWithRoleDepartmentHead() throws Exception {

        final Person departmentHeadPerson = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        departmentHeadPerson.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        departmentHeadPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(departmentHeadPerson);

        final Person person = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        person.setId(2L);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHeadPerson, person)).thenReturn(true);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID)).andExpect(status().isOk());
    }

    @Test
    void ensureGetSickNoteDetailsIsNotAccessibleForPersonWithDepartmentHeadForWrongUser() {

        final Person departmentHeadPerson = personWithRole(DEPARTMENT_HEAD);
        departmentHeadPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(departmentHeadPerson);
        final Person person = personWithRole(USER);
        person.setId(2L);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHeadPerson, person)).thenReturn(false);

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteDetailsAccessibleForPersonWithRoleSecondStageAuthority() throws Exception {

        final Person secondStageAuthority = personWithRole(SECOND_STAGE_AUTHORITY);
        secondStageAuthority.setId(1L);
        when(personService.getSignedInUser()).thenReturn(secondStageAuthority);
        final Person person = personWithRole(USER);
        person.setId(2L);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, person)).thenReturn(true);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID)).andExpect(status().isOk());
    }

    @Test
    void ensureGetSickNoteDetailsAccessibleForPersonWithRoleSecondStageAuthorityAndSickNoteView() throws Exception {

        final Person secondStageAuthority = personWithRole(SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW);
        secondStageAuthority.setId(1L);
        when(personService.getSignedInUser()).thenReturn(secondStageAuthority);
        final Person person = personWithRole(USER);
        person.setId(2L);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, person)).thenReturn(true);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID)).andExpect(status().isOk());
    }

    @Test
    void ensureGetSickNoteDetailsIsNotAccessibleForPersonWithSecondStageAuthorityForWrongUser() {

        final Person secondStageAuthority = personWithRole(SECOND_STAGE_AUTHORITY);
        secondStageAuthority.setId(1L);
        when(personService.getSignedInUser()).thenReturn(secondStageAuthority);
        final Person person = personWithRole(USER);
        person.setId(2L);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, person)).thenReturn(false);

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteDetailsNotAccessibleForOtherPersonIfNotRoleOffice() {

        final int somePersonId = 1;
        when(personService.getSignedInUser()).thenReturn(personWithId(somePersonId));

        final int anotherPersonId = 2;
        Person somePerson = personWithId(anotherPersonId);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID))
            .thenReturn(Optional.of(SickNote.builder().person(somePerson).build()));

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteDetailsProvidesCorrectModelAttributesAndView() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));

        final Person person = new Person();
        final SickNote sickNote = SickNote.builder()
            .person(person)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));

        when(sickNoteCommentService.getCommentsBySickNote(any(SickNote.class))).thenReturn(List.of());

        final Department department = new Department();
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(department));

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(model().attribute("sickNote", instanceOf(SickNote.class)))
            .andExpect(model().attribute("comment", instanceOf(SickNoteCommentFormDto.class)))
            .andExpect(model().attribute("comments", instanceOf(List.class)))
            .andExpect(model().attribute("departmentsOfPerson", List.of(department)))
            .andExpect(view().name("sicknote/sick_note"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void ensureGetSickNoteDetailsCanEditSickNotesWithRole(Role role) throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(role, SICK_NOTE_VIEW, SICK_NOTE_EDIT));
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));
        when(sickNoteCommentService.getCommentsBySickNote(any(SickNote.class))).thenReturn(List.of());

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(view().name("sicknote/sick_note"))
            .andExpect(model().attribute("canEditSickNote", true));
    }

    @Test
    void ensureGetSickNoteDetailsCanEditSickNotesDepartmentHead() throws Exception {

        final Person departmentHead = personWithRole(DEPARTMENT_HEAD, SICK_NOTE_VIEW, SICK_NOTE_EDIT);
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final Person person = new Person();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));
        when(sickNoteCommentService.getCommentsBySickNote(any(SickNote.class))).thenReturn(List.of());
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(view().name("sicknote/sick_note"))
            .andExpect(model().attribute("canEditSickNote", true));
    }

    @Test
    void ensureGetSickNoteDetailsCanNotEditSickNotesDepartmentHeadOfDifferentDepartment() {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

        final Person departmentHead = personWithRole(DEPARTMENT_HEAD);
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final Person person = new Person();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(false);

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteDetailsCanEditSickNotesSecondStageAuthority() throws Exception {

        final Person ssa = personWithRole(SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW, SICK_NOTE_EDIT);
        when(personService.getSignedInUser()).thenReturn(ssa);

        final Person person = new Person();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));
        when(sickNoteCommentService.getCommentsBySickNote(any(SickNote.class))).thenReturn(List.of());
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, person)).thenReturn(true);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(view().name("sicknote/sick_note"))
            .andExpect(model().attribute("canEditSickNote", true));
    }

    @Test
    void ensureGetSickNoteDetailsCanNotEditSickNotesSecondStageAuthorityOfDifferentDepartment() {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

        final Person ssa = personWithRole(SECOND_STAGE_AUTHORITY);
        when(personService.getSignedInUser()).thenReturn(ssa);

        final Person person = new Person();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(person).build()));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, person)).thenReturn(false);

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureGetSickNoteDetailsCanConvertSickNotes() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));
        when(sickNoteCommentService.getCommentsBySickNote(any(SickNote.class))).thenReturn(List.of());

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(view().name("sicknote/sick_note"))
            .andExpect(model().attribute("canConvertSickNote", true));
    }

    @Test
    void ensureGetSickNoteDetailsCanDeleteSickNotes() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));
        when(sickNoteCommentService.getCommentsBySickNote(any(SickNote.class))).thenReturn(List.of());

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(view().name("sicknote/sick_note"))
            .andExpect(model().attribute("canDeleteSickNote", true));
    }

    @Test
    void ensureGetSickNoteDetailsCanCommentSickNotes() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));
        when(sickNoteCommentService.getCommentsBySickNote(any(SickNote.class))).thenReturn(List.of());

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID))
            .andExpect(view().name("sicknote/sick_note"))
            .andExpect(model().attribute("canCommentSickNote", true));
    }

    @Test
    void ensurePostNewSickNoteShowsFormIfValidationFails() throws Exception {

        when(personService.getSignedInUser()).thenReturn(personWithRole(OFFICE));
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));
        final Person sickNotePerson = new Person();
        when(personService.getActivePersons()).thenReturn(List.of(sickNotePerson));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(sickNotePerson));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteValidator).validate(any(), any());

        perform(post("/web/sicknote").param("person", "1"))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(model().attribute("person", sickNotePerson))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void ensurePostNewSickNoteShowsFormWithAllActivePersonsWhenValidationFails(Role role) throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, role));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person john = new Person("john", "Doe", "John", "john@example.org");
        john.setId(1L);

        final Person jane = new Person("jane", "Doe", "Jane", "jane@example.org");
        jane.setId(2L);

        when(personService.getActivePersons()).thenReturn(List.of(jane, john));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteValidator).validate(any(), any());

        perform(post("/web/sicknote"))
            .andExpect(model().attribute("persons", equalTo(List.of(jane, john))));
    }

    @Test
    void ensurePostNewSickNoteShowsFormForDepartmentHeadWhenValidationFails() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person john = new Person("john", "Doe", "John", "john@example.org");
        john.setId(1L);

        final Person jane = new Person("jane", "Doe", "Jane", "jane@example.org");
        jane.setId(2L);

        final Person inactivePerson = new Person("inactive", "", "", "");
        inactivePerson.setPermissions(List.of(INACTIVE));
        inactivePerson.setId(3L);

        when(departmentService.getManagedMembersOfDepartmentHead(signedInUser)).thenReturn(List.of(jane, john, inactivePerson));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteValidator).validate(any(), any());

        perform(post("/web/sicknote"))
            .andExpect(model().attribute("persons", equalTo(List.of(jane, john))));
    }

    @Test
    void ensurePostNewSickNoteShowsFormForSecondStageAuthorityWhenValidationFails() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person john = new Person("john", "Doe", "John", "john@example.org");
        john.setId(1L);

        final Person jane = new Person("jane", "Doe", "Jane", "jane@example.org");
        jane.setId(2L);

        final Person inactivePerson = new Person("inactive", "", "", "");
        inactivePerson.setPermissions(List.of(INACTIVE));
        inactivePerson.setId(3L);

        when(departmentService.getManagedMembersForSecondStageAuthority(signedInUser)).thenReturn(List.of(jane, john, inactivePerson));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteValidator).validate(any(), any());

        perform(post("/web/sicknote"))
            .andExpect(model().attribute("persons", equalTo(List.of(jane, john))));
    }

    @Test
    void ensurePostNewSickNoteShowsFormForDepartmentHeadAndSecondStageAuthorityWhenValidationFails() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY, SICK_NOTE_ADD));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person john = new Person("john", "Doe", "John", "john@example.org");
        john.setId(1L);

        final Person jane = new Person("jane", "Doe", "Jane", "jane@example.org");
        jane.setId(2L);

        final Person distinctPerson = new Person("distinct", "", "", "");
        distinctPerson.setId(3L);

        final Person inactivePerson = new Person("inactive", "", "", "");
        inactivePerson.setPermissions(List.of(INACTIVE));
        inactivePerson.setId(4L);

        when(departmentService.getManagedMembersOfDepartmentHead(signedInUser)).thenReturn(List.of(jane, inactivePerson, distinctPerson));
        when(departmentService.getManagedMembersForSecondStageAuthority(signedInUser)).thenReturn(List.of(distinctPerson, john));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteValidator).validate(any(), any());

        perform(post("/web/sicknote"))
            .andExpect(model().attribute("persons", equalTo(List.of(distinctPerson, jane, john))));
    }

    @Test
    void ensurePostNewSickNoteCreatesSickNoteIfValidationSuccessful() throws Exception {

        userIsAllowedToSubmitSickNotes(false);

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(sickNoteInteractionService.create(any(SickNote.class), eq(signedInPerson), eq(null)))
            .thenReturn(SickNote.builder().id(42L).build());

        perform(post("/web/sicknote/").param("person.id", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/sicknote/42"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"25.03.2022", "25.03.22", "25.3.2022", "25.3.22", "1.4.22"})
    void ensureCreateSickNoteSucceedsWithDate(String givenDate) throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(sickNoteInteractionService.create(any(SickNote.class), eq(signedInPerson), eq(null)))
            .thenReturn(SickNote.builder().id(42L).build());

        perform(
            post("/web/sicknote/")
                .param("startDate", givenDate)
                .param("endDate", givenDate)
                .param("aubStartDate", givenDate)
                .param("aubEndDate", givenDate)
                    .param("person.id", "2")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/sicknote/42"));
    }

    @Test
    void ensurePostNewSickNoteRedirectsToCreatedSickNote() throws Exception {

        when(personService.getSignedInUser()).thenReturn(new Person());

        doAnswer(invocation -> SickNote.builder(invocation.getArgument(0)).id(SOME_SICK_NOTE_ID).build())
            .when(sickNoteInteractionService).create(any(SickNote.class), any(Person.class), any());

        perform(post("/web/sicknote/").param("person.id", "1"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    void editPostSickNoteShowsFormIfValidationFails() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));
        when(vacationTypeViewModelService.getVacationTypeColors()).thenReturn(List.of(new VacationTypeDto(1L, ORANGE)));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteValidator).validate(any(), any());

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit")
                .param("person", "1"))
            .andExpect(model().attribute("vacationTypeColors", equalTo(List.of(new VacationTypeDto(1L, ORANGE)))))
            .andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    void editPostSickNoteUpdatesSickNoteIfValidationSuccessful() throws Exception {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"));

        verify(sickNoteInteractionService).update(any(SickNote.class), eq(signedInPerson), any());
    }

    @Test
    void editPostSickNoteRedirectsToCreatedSickNote() throws Exception {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    void editPostSickNoteThrowsUnknownSickNoteException() {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/edit"))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensurePostAddCommentThrowsUnknownSickNoteException() {
        assertThatThrownBy(() ->
            perform(post("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/comment"))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensurePostAddCommentAddsFlashAttributeAndRedirectsToSickNoteIfValidationFails() throws Exception {

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(USER, BOSS, SICK_NOTE_COMMENT));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("text", "errors");
            return null;
        }).when(sickNoteCommentFormValidator).validate(any(SickNoteCommentFormDto.class), any(Errors.class));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/comment"))
            .andExpect(flash().attribute("errors", instanceOf(Errors.class)))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    void ensurePostAddCommentCreatesSickNoteCommentIfValidationSuccessful() throws Exception {

        final Person signedInPerson = new Person();
        signedInPerson.setPermissions(List.of(USER, BOSS, SICK_NOTE_COMMENT));
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/comment"));

        verify(sickNoteCommentService).create(any(SickNote.class), any(), eq(signedInPerson), any());
    }

    @Test
    void ensureGetSickNoteCannotBeCommentedWithoutPermissions() {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .status(ACTIVE)
            .person(signedInUser)
            .build();

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));

        assertThatThrownBy(() ->
            perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/comment"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ensureRedirectToSickNoteAfterConvert() throws Exception {

        final SickNote sickNote = SickNote.builder()
            .person(new Person())
            .status(ACTIVE)
            .build();

        when(vacationTypeService.getById(42L)).thenReturn(Optional.of(ProvidedVacationType.builder(new StaticMessageSource()).build()));
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert").param("vacationType", "42"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    void ensureRedirectToSickNoteAfterComment() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, BOSS, SICK_NOTE_COMMENT));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final SickNote sickNote = SickNote.builder()
            .id(15L)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/comment"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/sicknote/" + SOME_SICK_NOTE_ID));
    }

    @Test
    void ensureRedirectToSickNoteAfterCancel() throws Exception {

        final Person person = new Person();
        person.setPermissions(List.of(USER, BOSS, SICK_NOTE_CANCEL));
        when(personService.getSignedInUser()).thenReturn(person);

        final SickNote sickNote = SickNote.builder()
            .id(15L)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(sickNoteInteractionService.cancel(sickNote, person)).thenReturn(sickNote);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/cancel"))
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

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(CANCELLED).build()));

        assertThatThrownBy(() ->
            perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
        ).hasCauseInstanceOf(SickNoteAlreadyInactiveException.class);
    }

    @Test
    void ensureGetConvertSickNoteToVacationAddModel() throws Exception {

        overtimeActive(false);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

        final List<VacationType<?>> vacationTypes = List.of(ProvidedVacationType.builder(new StaticMessageSource()).build());
//        final List<VacationType<?>> vacationTypes = List.of(new VacationType(1L, true, HOLIDAY, "message_key", true, true, YELLOW, false));
        when(vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME)).thenReturn(vacationTypes);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(model().attribute("sickNote", instanceOf(SickNote.class)))
            .andExpect(model().attribute("sickNoteConvertForm", instanceOf(SickNoteConvertForm.class)))
            .andExpect(model().attribute("vacationTypes", vacationTypes));
    }

    @Test
    void ensureGetConvertSickNoteToVacationAddModelOvertimeTrue() throws Exception {

        overtimeActive(true);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

        final List<VacationType<?>> vacationTypes = List.of(ProvidedVacationType.builder(new StaticMessageSource()).build());
//        final List<VacationType<?>> vacationTypes = List.of(new VacationType(1L, true, HOLIDAY, "message_key", true, true, YELLOW, false));
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        perform(get("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(model().attribute("sickNote", instanceOf(SickNote.class)))
            .andExpect(model().attribute("sickNoteConvertForm", instanceOf(SickNoteConvertForm.class)))
            .andExpect(model().attribute("vacationTypes", vacationTypes));
    }

    @Test
    void ensureGetConvertSickNoteToVacationUsesCorrectView() throws Exception {

        overtimeActive(false);

        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

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
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

        final List<VacationType<?>> vacationTypes = List.of(ProvidedVacationType.builder(new StaticMessageSource()).build());
//        final List<VacationType<?>> vacationTypes = List.of(new VacationType(1L, true, HOLIDAY, "message_key", true, true, YELLOW, false));
        when(vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME)).thenReturn(vacationTypes);

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteConvertFormValidator).validate(any(), any());

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(view().name("sicknote/sick_note_convert"))
            .andExpect(model().attribute("errors", instanceOf(Errors.class)))
            .andExpect(model().attribute("sickNote", instanceOf(SickNote.class)))
            .andExpect(model().attribute("sickNoteConvertForm", instanceOf(SickNoteConvertForm.class)))
            .andExpect(model().attribute("vacationTypes", vacationTypes));
    }

    @Test
    void ensurePostConvertSickNoteToVacationFilledModelCorrectlyAndViewIfValidationFailsWithOvertimeActive() throws Exception {

        overtimeActive(true);
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(SickNote.builder().person(new Person()).status(ACTIVE).build()));

        final List<VacationType<?>> vacationTypes = List.of(ProvidedVacationType.builder(new StaticMessageSource()).build());
//        final List<VacationType<?>> vacationTypes = List.of(new VacationType(1L, true, HOLIDAY, "message_key", true, true, YELLOW, false));
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("person", "error");
            return null;
        }).when(sickNoteConvertFormValidator).validate(any(), any());

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert"))
            .andExpect(view().name("sicknote/sick_note_convert"))
            .andExpect(model().attribute("errors", instanceOf(Errors.class)))
            .andExpect(model().attribute("sickNote", instanceOf(SickNote.class)))
            .andExpect(model().attribute("sickNoteConvertForm", instanceOf(SickNoteConvertForm.class)))
            .andExpect(model().attribute("vacationTypes", vacationTypes));
    }

    @Test
    void ensurePostConvertSickNoteToVacationConvertsSickNoteIfValidationSuccessful() throws Exception {

        final SickNote sickNote = SickNote.builder()
            .person(new Person())
            .status(ACTIVE)
            .build();

        when(vacationTypeService.getById(42L)).thenReturn(Optional.of(ProvidedVacationType.builder(new StaticMessageSource()).build()));
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));

        final Person signedInPerson = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInPerson);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/convert")
            .param("vacationType", "42")
        );

        verify(sickNoteInteractionService).convert(any(SickNote.class), any(Application.class), eq(signedInPerson));
    }

    @Test
    void ensureCancelSickNoteThrowsUnknownSickNoteException() {

        assertThatThrownBy(() ->
            perform(post("/web/sicknote/" + UNKNOWN_SICK_NOTE_ID + "/cancel"))
        ).hasCauseInstanceOf(UnknownSickNoteException.class);
    }

    @Test
    void ensureCancelSickNoteCancelsSickNoteCorrectlyByBoss() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, BOSS, SICK_NOTE_CANCEL));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final SickNote sickNote = SickNote.builder().id(1L).status(ACTIVE).build();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(sickNoteInteractionService.cancel(sickNote, signedInUser)).thenReturn(sickNote);
        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/cancel"))
            .andExpect(view().name("redirect:/web/sicknote/1"));
    }

    @Test
    void ensureCancelSickNoteCancelsSickNoteCorrectlyByDH() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_CANCEL));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final SickNote sickNote = SickNote.builder().id(1L).status(ACTIVE).build();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(sickNoteInteractionService.cancel(sickNote, signedInUser)).thenReturn(sickNote);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, sickNote.getPerson())).thenReturn(true);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/cancel"))
            .andExpect(view().name("redirect:/web/sicknote/1"));
    }

    @Test
    void ensureCancelSickNoteCancelsSickNoteCorrectlyBySSA() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_CANCEL));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final SickNote sickNote = SickNote.builder().id(1L).status(ACTIVE).build();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(sickNoteInteractionService.cancel(sickNote, signedInUser)).thenReturn(sickNote);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, sickNote.getPerson())).thenReturn(true);

        perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/cancel"))
            .andExpect(view().name("redirect:/web/sicknote/1"));
    }

    @Test
    void ensureGetSickNoteCannotBeCancelledWithoutPermissions() {

        final Person signedInUser = new Person();
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final SickNote sickNote = SickNote.builder().person(new Person()).status(ACTIVE).id(1L).build();
        when(sickNoteService.getById(SOME_SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));

        assertThatThrownBy(() ->
            perform(post("/web/sicknote/" + SOME_SICK_NOTE_ID + "/cancel"))
        ).hasCauseInstanceOf(AccessDeniedException.class);
    }

    private void userIsAllowedToSubmitSickNotes(boolean allowed) {
        var sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setUserIsAllowedToSubmitSickNotes(allowed);
        var settings = new Settings();
        settings.setSickNoteSettings(sickNoteSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private void overtimeActive(boolean active) {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(active);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private Person personWithRole(Role... role) {
        final Person person = new Person();
        person.setPermissions(List.of(role));
        return person;
    }

    private Person personWithId(long personId) {
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
