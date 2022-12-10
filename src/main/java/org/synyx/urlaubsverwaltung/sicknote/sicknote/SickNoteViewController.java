package org.synyx.urlaubsverwaltung.sicknote.sicknote;

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
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypePropertyEditor;
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
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentForm;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentFormValidator;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.web.InstantPropertyEditor;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
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
class SickNoteViewController {

    private final SickNoteService sickNoteService;
    private final SickNoteInteractionService sickNoteInteractionService;
    private final SickNoteCommentService sickNoteCommentService;
    private final SickNoteTypeService sickNoteTypeService;
    private final VacationTypeService vacationTypeService;
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final PersonService personService;
    private final DepartmentService departmentService;
    private final WorkDaysCountService workDaysCountService;
    private final SickNoteValidator sickNoteValidator;
    private final SickNoteCommentFormValidator sickNoteCommentFormValidator;
    private final SickNoteConvertFormValidator sickNoteConvertFormValidator;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    SickNoteViewController(SickNoteService sickNoteService, SickNoteInteractionService sickNoteInteractionService,
                           SickNoteCommentService sickNoteCommentService, SickNoteTypeService sickNoteTypeService,
                           VacationTypeService vacationTypeService, VacationTypeViewModelService vacationTypeViewModelService, PersonService personService,
                           DepartmentService departmentService, WorkDaysCountService workDaysCountService, SickNoteValidator sickNoteValidator,
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
        this.workDaysCountService = workDaysCountService;
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
        binder.registerCustomEditor(VacationType.class, new VacationTypePropertyEditor(vacationTypeService));
    }

    @GetMapping("/sicknote/{id}")
    public String sickNoteDetails(@PathVariable("id") Integer id, Model model) throws UnknownSickNoteException {

        final Person signedInUser = personService.getSignedInUser();
        final SickNote sickNote = getSickNote(id);

        final boolean isSamePerson = sickNote.getPerson().equals(signedInUser);

        if (isSamePerson
            || signedInUser.hasRole(OFFICE)
            || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_VIEW, sickNote)
            || departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, sickNote.getPerson())
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, sickNote.getPerson())) {
            model.addAttribute("sickNote", new ExtendedSickNote(sickNote, workDaysCountService));
            model.addAttribute("comment", new SickNoteCommentForm());

            final List<SickNoteCommentEntity> comments = sickNoteCommentService.getCommentsBySickNote(sickNote);
            model.addAttribute("comments", comments);

            model.addAttribute("canEditSickNote", signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_EDIT, sickNote));
            model.addAttribute("canConvertSickNote", signedInUser.hasRole(OFFICE));
            model.addAttribute("canDeleteSickNote", signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_CANCEL, sickNote));
            model.addAttribute("canCommentSickNote", signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_COMMENT, sickNote));

            model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(sickNote.getPerson()));

            return "thymeleaf/sicknote/sick_note";
        }

        throw new AccessDeniedException(format(
            "User '%s' has not the correct permissions to see the sick note of user '%s'",
            signedInUser.getId(), sickNote.getPerson().getId()));
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_ADD')")
    @GetMapping("/sicknote/new")
    public String newSickNote(@RequestParam(value = "person", required = false) Integer personId, Model model) throws UnknownPersonException {

        final Person signedInUser = personService.getSignedInUser();

        final Person person;
        if (personId != null) {
            person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        } else {
            person = signedInUser;
        }

        model.addAttribute("signedInUser", signedInUser);
        model.addAttribute("person", person);

        model.addAttribute("sickNote", new SickNoteForm());

        final List<Person> managedPersons = getManagedPersons(signedInUser);
        model.addAttribute("persons", managedPersons);
        model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

        addVacationTypeColorsToModel(model);

        return "thymeleaf/sicknote/sick_note_form";
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_ADD')")
    @PostMapping("/sicknote")
    public String addNewSickNote(@ModelAttribute("sickNote") SickNoteForm sickNoteForm, Errors errors, Model model) {

        final Person signedInUser = personService.getSignedInUser();
        model.addAttribute("signedInUser", signedInUser);

        final SickNote sickNote = SickNote.builder(sickNoteForm.generateSickNote())
                .applier(signedInUser)
                .build();

        sickNoteValidator.validate(sickNote, errors);
        if (errors.hasErrors()) {
            model.addAttribute("errors", errors);
            model.addAttribute("sickNote", sickNoteForm);
            model.addAttribute("person", sickNoteForm.getPerson());
            model.addAttribute("persons", getManagedPersons(signedInUser));
            model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

            addVacationTypeColorsToModel(model);

            return "thymeleaf/sicknote/sick_note_form";
        }

        final SickNote updatedSickNote = sickNoteInteractionService.create(sickNote, signedInUser, sickNoteForm.getComment());

        return "redirect:/web/sicknote/" + updatedSickNote.getId();
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_EDIT')")
    @GetMapping("/sicknote/{id}/edit")
    public String editSickNote(@PathVariable("id") Integer id, Model model) throws UnknownSickNoteException, SickNoteAlreadyInactiveException {

        final SickNote sickNote = getSickNote(id);
        if (!sickNote.isActive()) {
            throw new SickNoteAlreadyInactiveException(id);
        }

        final Person signedInUser = personService.getSignedInUser();
        if (!signedInUser.hasRole(OFFICE) && !isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_EDIT, sickNote)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to edit the sick note of user '%s'",
                signedInUser.getId(), sickNote.getPerson().getId()));
        }

        final SickNoteForm sickNoteForm = new SickNoteForm(sickNote);
        model.addAttribute("sickNote", sickNoteForm);
        model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

        addVacationTypeColorsToModel(model);

        return "thymeleaf/sicknote/sick_note_form";
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_EDIT')")
    @PostMapping("/sicknote/{id}/edit")
    public String editSickNote(@PathVariable("id") Integer sickNoteId,
                               @ModelAttribute("sickNote") SickNoteForm sickNoteForm, Errors errors, Model model) throws UnknownSickNoteException {

        final Optional<SickNote> maybeSickNote = sickNoteService.getById(sickNoteId);
        if (maybeSickNote.isEmpty()) {
            throw new UnknownSickNoteException(sickNoteId);
        }
        final SickNote persistedSickNote = maybeSickNote.get();
        final SickNote editedSickNote = merge(persistedSickNote, sickNoteForm);
        sickNoteValidator.validate(editedSickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute("errors", errors);
            model.addAttribute("sickNote", sickNoteForm);
            model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

            addVacationTypeColorsToModel(model);

            return "thymeleaf/sicknote/sick_note_form";
        }

        final Person signedInUser = personService.getSignedInUser();
        sickNoteInteractionService.update(editedSickNote, signedInUser, sickNoteForm.getComment());

        return "redirect:/web/sicknote/" + sickNoteId;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_COMMENT')")
    @PostMapping("/sicknote/{id}/comment")
    public String addComment(@PathVariable("id") Integer id,
                             @ModelAttribute("comment") SickNoteCommentForm comment, Errors errors, RedirectAttributes redirectAttributes)
        throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        final Person signedInUser = personService.getSignedInUser();

        if (!signedInUser.hasRole(OFFICE) && !isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_COMMENT, sickNote)) {
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
    public String convertSickNoteToVacation(@PathVariable("id") Integer id, Model model)
        throws UnknownSickNoteException, SickNoteAlreadyInactiveException {

        final SickNote sickNote = getSickNote(id);
        if (!sickNote.isActive()) {
            throw new SickNoteAlreadyInactiveException(id);
        }

        model.addAttribute("sickNote", new ExtendedSickNote(sickNote, workDaysCountService));
        model.addAttribute("sickNoteConvertForm", new SickNoteConvertForm(sickNote));
        model.addAttribute("vacationTypes", getActiveVacationTypes());

        return "thymeleaf/sicknote/sick_note_convert";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/sicknote/{id}/convert")
    public String convertSickNoteToVacation(@PathVariable("id") Integer id,
                                            @ModelAttribute("sickNoteConvertForm") SickNoteConvertForm sickNoteConvertForm, Errors errors, Model model)
        throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        sickNoteConvertFormValidator.validate(sickNoteConvertForm, errors);

        if (errors.hasErrors()) {
            model.addAttribute("errors", errors);
            model.addAttribute("sickNote", new ExtendedSickNote(sickNote, workDaysCountService));
            model.addAttribute("sickNoteConvertForm", sickNoteConvertForm);
            model.addAttribute("vacationTypes", getActiveVacationTypes());

            return "thymeleaf/sicknote/sick_note_convert";
        }

        sickNoteInteractionService.convert(sickNote, sickNoteConvertForm.generateApplicationForLeave(clock), personService.getSignedInUser());

        return "redirect:/web/sicknote/" + id;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_CANCEL')")
    @PostMapping("/sicknote/{id}/cancel")
    public String cancelSickNote(@PathVariable("id") Integer id) throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        final Person signedInUser = personService.getSignedInUser();

        if (!signedInUser.hasRole(OFFICE) && !isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_CANCEL, sickNote)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to cancel the sick note of user '%s'",
                signedInUser.getId(), sickNote.getPerson().getId()));
        }

        final SickNote cancelledSickNote = sickNoteInteractionService.cancel(sickNote, signedInUser);
        return "redirect:/web/sicknote/" + cancelledSickNote.getId();
    }

    private boolean isPersonAllowedToExecuteRoleOn(Person person, Role role, SickNote sickNote) {
        final boolean isBossOrDepartmentHeadOrSecondStageAuthority = person.hasRole(BOSS)
            || departmentService.isDepartmentHeadAllowedToManagePerson(person, sickNote.getPerson())
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(person, sickNote.getPerson());
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

    private List<VacationType> getActiveVacationTypes() {
        final List<VacationType> vacationTypes;

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

    private SickNote getSickNote(Integer id) throws UnknownSickNoteException {
        return sickNoteService.getById(id).orElseThrow(() -> new UnknownSickNoteException(id));
    }
}
