package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentEntity;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentFormDto;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentFormValidator;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.web.InstantPropertyEditor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
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
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteMapper.merge;

/**
 * Controller for {@link SickNote} purposes.
 */
@Controller
@RequestMapping("/web")
class SickNoteViewController implements HasLaunchpad {

    private final SickNoteService sickNoteService;
    private final SickNoteInteractionService sickNoteInteractionService;
    private final SickNoteCommentService sickNoteCommentService;
    private final SickNoteTypeService sickNoteTypeService;
    private final VacationTypeService vacationTypeService;
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final PersonService personService;
    private final DepartmentService departmentService;
    private final SickNoteValidator sickNoteValidator;
    private final SickNoteCommentFormValidator sickNoteCommentFormValidator;
    private final SickNoteConvertFormValidator sickNoteConvertFormValidator;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    SickNoteViewController(SickNoteService sickNoteService, SickNoteInteractionService sickNoteInteractionService,
                           SickNoteCommentService sickNoteCommentService, SickNoteTypeService sickNoteTypeService,
                           VacationTypeService vacationTypeService, VacationTypeViewModelService vacationTypeViewModelService, PersonService personService,
                           DepartmentService departmentService, SickNoteValidator sickNoteValidator,
                           SickNoteCommentFormValidator sickNoteCommentFormValidator, SickNoteConvertFormValidator sickNoteConvertFormValidator,
                           SettingsService settingsService, Clock clock) {

        this.sickNoteService = sickNoteService;
        this.sickNoteInteractionService = sickNoteInteractionService;
        this.sickNoteCommentService = sickNoteCommentService;
        this.sickNoteTypeService = sickNoteTypeService;
        this.vacationTypeService = vacationTypeService;
        this.vacationTypeViewModelService = vacationTypeViewModelService;
        this.personService = personService;
        this.departmentService = departmentService;
        this.sickNoteValidator = sickNoteValidator;
        this.sickNoteCommentFormValidator = sickNoteCommentFormValidator;
        this.sickNoteConvertFormValidator = sickNoteConvertFormValidator;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(Instant.class, new InstantPropertyEditor(clock, settingsService));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }

    @GetMapping("/sicknote/{id}")
    public String sickNoteDetails(@PathVariable("id") Long id, Model model) throws UnknownSickNoteException {

        final Person signedInUser = personService.getSignedInUser();
        final SickNote sickNote = getSickNote(id);
        final Person sickNotePerson = sickNote.getPerson();

        final boolean isSamePerson = sickNotePerson.equals(signedInUser);

        if (isSamePerson
            || signedInUser.hasRole(OFFICE)
            || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_VIEW, sickNotePerson)
            || departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, sickNotePerson)
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, sickNotePerson)) {
            model.addAttribute("sickNote", sickNote);
            model.addAttribute("comment", new SickNoteCommentFormDto());

            final List<SickNoteCommentEntity> comments = sickNoteCommentService.getCommentsBySickNote(sickNote);
            model.addAttribute("comments", comments);

            model.addAttribute("canAcceptSickNote", signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_EDIT, sickNotePerson));
            model.addAttribute("canEditSickNote", signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_EDIT, sickNotePerson));
            model.addAttribute("canConvertSickNote", signedInUser.hasRole(OFFICE));
            model.addAttribute("canDeleteSickNote", signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_CANCEL, sickNotePerson));
            model.addAttribute("canCommentSickNote", signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_COMMENT, sickNotePerson));

            model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(sickNotePerson));

            return "sicknote/sick_note";
        }

        throw new AccessDeniedException(format(
            "User '%s' has not the correct permissions to see the sick note of user '%s'",
            signedInUser.getId(), sickNotePerson.getId()));
    }

    @GetMapping("/sicknote/new")
    public String newSickNote(@RequestParam(value = "person", required = false) Long personId, Model model) throws UnknownPersonException {

        final Person signedInUser = personService.getSignedInUser();

        if (!signedInUser.hasAnyRole(OFFICE, SICK_NOTE_ADD) && !settingsService.getSettings().getSickNoteSettings().getUserIsAllowedToSubmitSickNotes()) {
            throw new AccessDeniedException(
                    "User '%s' has not the correct permissions to create a sick note".formatted(
                    signedInUser.getId()));
        }


        final Person person = personId == null
            ? signedInUser
            : personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        model.addAttribute("signedInUser", signedInUser);
        model.addAttribute("person", person);

        model.addAttribute("sickNote", new SickNoteFormDto());

        final List<Person> managedPersons = getManagedPersons(signedInUser);
        model.addAttribute("persons", managedPersons);
        model.addAttribute("canAddSickNote", canAddSickNote(signedInUser, person));
        model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

        addVacationTypeColorsToModel(model);

        return "sicknote/sick_note_form";
    }

    @PostMapping("/sicknote")
    public String addOrSubmitNewSickNote(@ModelAttribute("sickNote") SickNoteFormDto sickNoteFormDto, Errors errors, Model model) {

        final Person signedInUser = personService.getSignedInUser();
        model.addAttribute("signedInUser", signedInUser);
        final Person sickNotePerson = sickNoteFormDto.getPerson();

        final SickNote sickNote = SickNote.builder()
            .id(sickNoteFormDto.getId())
                .person(sickNotePerson)
            .applier(signedInUser)
            .sickNoteType(sickNoteFormDto.getSickNoteType())
            .startDate(sickNoteFormDto.getStartDate())
            .endDate(sickNoteFormDto.getEndDate())
            .dayLength(sickNoteFormDto.getDayLength())
            .aubStartDate(sickNoteFormDto.getAubStartDate())
            .aubEndDate(sickNoteFormDto.getAubEndDate())
            .build();


        sickNoteValidator.validate(sickNote, errors);
        if (errors.hasErrors()) {
            model.addAttribute("errors", errors);
            model.addAttribute("sickNote", sickNoteFormDto);
            model.addAttribute("person", sickNotePerson);
            model.addAttribute("persons", getManagedPersons(signedInUser));
            model.addAttribute("canAddSickNote", canAddSickNote(signedInUser, sickNotePerson));
            model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

            addVacationTypeColorsToModel(model);

            return "sicknote/sick_note_form";
        }

        final SickNote updatedSickNote;
        var isSubmission = sickNote.getPerson().equals(sickNote.getApplier()) && settingsService.getSettings().getSickNoteSettings().getUserIsAllowedToSubmitSickNotes();
        if (isSubmission) {
            updatedSickNote = sickNoteInteractionService.submit(sickNote, signedInUser, sickNoteFormDto.getComment());
        } else {
            updatedSickNote = sickNoteInteractionService.create(sickNote, signedInUser, sickNoteFormDto.getComment());
        }

        return "redirect:/web/sicknote/" + updatedSickNote.getId();
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_EDIT')")
    @GetMapping("/sicknote/{id}/edit")
    public String editSickNote(@PathVariable("id") Long id, Model model) throws UnknownSickNoteException, SickNoteAlreadyInactiveException {

        final SickNote sickNote = getSickNote(id);
        if (!sickNote.isSubmittedOrActive()) {
            throw new SickNoteAlreadyInactiveException(id);
        }

        final Person signedInUser = personService.getSignedInUser();
        final Person sickNotePerson = sickNote.getPerson();

        if (!signedInUser.hasRole(OFFICE) && !isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_EDIT, sickNotePerson)) {            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to edit the sick note of user '%s'",
                    signedInUser.getId(), sickNotePerson.getId()));
        }

        final SickNoteFormDto sickNoteFormDto = toSickNoteForm(sickNote);

        model.addAttribute("sickNote", sickNoteFormDto);
        model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());
        model.addAttribute("canAddSickNote", canAddSickNote(signedInUser, sickNotePerson));

        addVacationTypeColorsToModel(model);

        return "sicknote/sick_note_form";
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_EDIT')")
    @PostMapping("/sicknote/{id}/edit")
    public String editSickNote(@PathVariable("id") Long sickNoteId,
                               @ModelAttribute("sickNote") SickNoteFormDto sickNoteFormDto, Errors errors, Model model) throws UnknownSickNoteException {

        final Optional<SickNote> maybeSickNote = sickNoteService.getById(sickNoteId);
        if (maybeSickNote.isEmpty()) {
            throw new UnknownSickNoteException(sickNoteId);
        }

        final SickNote persistedSickNote = maybeSickNote.get();
        final SickNote editedSickNote = merge(persistedSickNote, sickNoteFormDto);
        sickNoteValidator.validate(editedSickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute("errors", errors);
            model.addAttribute("sickNote", sickNoteFormDto);
            model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

            final Person signedInUser = personService.getSignedInUser();
            model.addAttribute("canAddSickNote", canAddSickNote(signedInUser, editedSickNote.getPerson()));

            addVacationTypeColorsToModel(model);

            return "sicknote/sick_note_form";
        }

        final Person signedInUser = personService.getSignedInUser();
        sickNoteInteractionService.update(editedSickNote, signedInUser, sickNoteFormDto.getComment());

        return "redirect:/web/sicknote/" + sickNoteId;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_EDIT')")
    @PostMapping("/sicknote/{id}/accept")
    public String acceptSickNote(@PathVariable("id") Long sickNoteId) throws UnknownSickNoteException {

        final Optional<SickNote> maybeSickNote = sickNoteService.getById(sickNoteId);
        if (maybeSickNote.isEmpty()) {
            throw new UnknownSickNoteException(sickNoteId);
        }

        final Person signedInUser = personService.getSignedInUser();
        sickNoteInteractionService.accept(maybeSickNote.get(), signedInUser);

        return "redirect:/web/sicknote/" + sickNoteId;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_COMMENT')")
    @PostMapping("/sicknote/{id}/comment")
    public String addComment(@PathVariable("id") Long id,
                             @ModelAttribute("comment") SickNoteCommentFormDto comment, Errors errors, RedirectAttributes redirectAttributes)
        throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        final Person signedInUser = personService.getSignedInUser();

        if (!signedInUser.hasRole(OFFICE) && !isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_COMMENT, sickNote.getPerson())) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to comment the sick note of user '%s'",
                signedInUser.getId(), sickNote.getPerson().getId()));
        }

        sickNoteCommentFormValidator.validate(comment, errors);
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/web/sicknote/" + id;
        }

        sickNoteCommentService.create(sickNote, SickNoteCommentAction.COMMENTED, signedInUser, comment.getText());

        return "redirect:/web/sicknote/" + id;
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/sicknote/{id}/convert")
    public String convertSickNoteToVacation(@PathVariable("id") Long id, Model model)
        throws UnknownSickNoteException, SickNoteAlreadyInactiveException {

        final SickNote sickNote = getSickNote(id);
        if (!sickNote.isSubmittedOrActive()) {
            throw new SickNoteAlreadyInactiveException(id);
        }

        model.addAttribute("sickNote", sickNote);
        model.addAttribute("sickNoteConvertForm", new SickNoteConvertForm(sickNote));
        model.addAttribute("vacationTypes", getActiveVacationTypes());

        return "sicknote/sick_note_convert";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/sicknote/{id}/convert")
    public String convertSickNoteToVacation(@PathVariable("id") Long id,
                                            @ModelAttribute("sickNoteConvertForm") SickNoteConvertForm sickNoteConvertForm,
                                            Errors errors, Model model)
        throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        sickNoteConvertFormValidator.validate(sickNoteConvertForm, errors);

        if (errors.hasErrors()) {
            model.addAttribute("errors", errors);
            model.addAttribute("sickNote", sickNote);
            model.addAttribute("sickNoteConvertForm", sickNoteConvertForm);
            model.addAttribute("vacationTypes", getActiveVacationTypes());

            return "sicknote/sick_note_convert";
        }

        final Application application = generateApplicationForLeave(sickNoteConvertForm);
        sickNoteInteractionService.convert(sickNote, application, personService.getSignedInUser());

        return "redirect:/web/sicknote/" + id;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_CANCEL')")
    @PostMapping("/sicknote/{id}/cancel")
    public String cancelSickNote(@PathVariable("id") Long id) throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        final Person signedInUser = personService.getSignedInUser();

        if (!signedInUser.hasRole(OFFICE) && !isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_CANCEL, sickNote.getPerson())) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to cancel the sick note of user '%s'",
                signedInUser.getId(), sickNote.getPerson().getId()));
        }

        final SickNote cancelledSickNote = sickNoteInteractionService.cancel(sickNote, signedInUser);
        return "redirect:/web/sicknote/" + cancelledSickNote.getId();
    }

    private boolean canAddSickNote(Person person, Person sickNotePerson) {
        return person.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(person, SICK_NOTE_ADD, sickNotePerson);
    }

    private boolean isPersonAllowedToExecuteRoleOn(Person person, Role role, Person sickNotePerson) {
        final boolean isBossOrDepartmentHeadOrSecondStageAuthority = person.hasRole(BOSS)
                || departmentService.isDepartmentHeadAllowedToManagePerson(person, sickNotePerson)
                || departmentService.isSecondStageAuthorityAllowedToManagePerson(person, sickNotePerson);
        return person.hasRole(role) && isBossOrDepartmentHeadOrSecondStageAuthority;
    }

    private List<Person> getManagedPersons(Person signedInUser) {

        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            return personService.getActivePersons();
        }

        final List<Person> membersForDepartmentHead = signedInUser.hasRole(DEPARTMENT_HEAD)
            ? departmentService.getManagedMembersOfDepartmentHead(signedInUser)
            : List.of();

        final List<Person> memberForSecondStageAuthority = signedInUser.hasRole(SECOND_STAGE_AUTHORITY)
            ? departmentService.getManagedMembersForSecondStageAuthority(signedInUser)
            : List.of();

        return Stream.concat(memberForSecondStageAuthority.stream(), membersForDepartmentHead.stream())
            .filter(person -> !person.hasRole(INACTIVE))
            .distinct()
            .sorted(comparing(Person::getFirstName).thenComparing(Person::getLastName))
            .collect(toList());
    }

    private List<VacationType<?>> getActiveVacationTypes() {
        final List<VacationType<?>> vacationTypes;

        final boolean overtimeActive = settingsService.getSettings().getOvertimeSettings().isOvertimeActive();
        if (overtimeActive) {
            vacationTypes = vacationTypeService.getActiveVacationTypes();
        } else {
            vacationTypes = vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME);
        }
        return vacationTypes;
    }

    private void addVacationTypeColorsToModel(Model model) {
        final List<VacationTypeDto> vacationTypeDtos = vacationTypeViewModelService.getVacationTypeColors();
        model.addAttribute("vacationTypeColors", vacationTypeDtos);
    }

    private SickNote getSickNote(Long id) throws UnknownSickNoteException {
        return sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));
    }

    private static SickNoteFormDto toSickNoteForm(SickNote sickNote) {
        return SickNoteFormDto.builder()
            .id(sickNote.getId())
            .person(sickNote.getPerson())
            .sickNoteType(sickNote.getSickNoteType())
            .startDate(sickNote.getStartDate())
            .endDate(sickNote.getEndDate())
            .dayLength(sickNote.getDayLength())
            .aubStartDate(sickNote.getAubStartDate())
            .aubEndDate(sickNote.getAubEndDate())
            .build();
    }

    private Application generateApplicationForLeave(SickNoteConvertForm sickNoteConvertForm) {

        final Long vacationTypeId = sickNoteConvertForm.getVacationType();
        final VacationType<?> vacationType = vacationTypeService.getById(vacationTypeId)
            .orElseThrow(() -> new IllegalStateException("vacationType with id=%s does not exist.".formatted(vacationTypeId)));

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(sickNoteConvertForm.getPerson());
        applicationForLeave.setVacationType(vacationType);
        applicationForLeave.setDayLength(sickNoteConvertForm.getDayLength());
        applicationForLeave.setStartDate(sickNoteConvertForm.getStartDate());
        applicationForLeave.setEndDate(sickNoteConvertForm.getEndDate());
        applicationForLeave.setReason(sickNoteConvertForm.getReason());
        applicationForLeave.setStatus(ALLOWED);
        applicationForLeave.setApplicationDate(LocalDate.now(clock));
        applicationForLeave.setEditedDate(LocalDate.now(clock));

        return applicationForLeave;
    }
}
