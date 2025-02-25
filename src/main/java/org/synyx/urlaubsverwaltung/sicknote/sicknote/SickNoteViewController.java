package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.slf4j.Logger;
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
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtendPreviewDto;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtension;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteTypeService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNullElse;
import static org.slf4j.LoggerFactory.getLogger;
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
    private final SickNoteExtensionService sickNoteExtensionService;
    private final SickNoteExtensionInteractionService sickNoteExtensionInteractionService;
    private final VacationTypeService vacationTypeService;
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final PersonService personService;
    private final DepartmentService departmentService;
    private final SickNoteValidator sickNoteValidator;
    private final SickNoteCommentFormValidator sickNoteCommentFormValidator;
    private final SickNoteConvertFormValidator sickNoteConvertFormValidator;
    private final SettingsService settingsService;
    private final Clock clock;

    private static final Logger LOG = getLogger(lookup().lookupClass());


    SickNoteViewController(
        SickNoteService sickNoteService,
        SickNoteInteractionService sickNoteInteractionService,
        SickNoteCommentService sickNoteCommentService,
        SickNoteTypeService sickNoteTypeService,
        SickNoteExtensionService sickNoteExtensionService,
        SickNoteExtensionInteractionService sickNoteExtensionInteractionService,
        VacationTypeService vacationTypeService,
        VacationTypeViewModelService vacationTypeViewModelService,
        PersonService personService,
        DepartmentService departmentService,
        SickNoteValidator sickNoteValidator,
        SickNoteCommentFormValidator sickNoteCommentFormValidator,
        SickNoteConvertFormValidator sickNoteConvertFormValidator,
        SettingsService settingsService,
        Clock clock
    ) {
        this.sickNoteService = sickNoteService;
        this.sickNoteInteractionService = sickNoteInteractionService;
        this.sickNoteCommentService = sickNoteCommentService;
        this.sickNoteTypeService = sickNoteTypeService;
        this.sickNoteExtensionService = sickNoteExtensionService;
        this.sickNoteExtensionInteractionService = sickNoteExtensionInteractionService;
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
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }

    @GetMapping("/sicknote/{id}")
    public String sickNoteDetails(@PathVariable("id") Long id,
                                  @RequestParam(value = "action", required = false) String action,
                                  @RequestParam(value = "shortcut", required = false) boolean shortcut,
                                  @RequestParam(value = "redirect", required = false) String redirect,
                                  Model model) throws UnknownSickNoteException {

        final Person signedInUser = personService.getSignedInUser();
        final SickNote sickNote = getSickNote(id);
        final Person sickNotePerson = sickNote.getPerson();

        final boolean isSamePerson = sickNotePerson.equals(signedInUser);
        final boolean isOffice = signedInUser.hasRole(OFFICE);

        if (isSamePerson
            || isOffice
            || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_VIEW, sickNotePerson)
            || departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, sickNotePerson)
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, sickNotePerson)) {

            final boolean isAllowedToEditSickNote = isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_EDIT, sickNotePerson);

            model.addAttribute("sickNote", sickNote);
            model.addAttribute("comment", new SickNoteCommentFormDto());

            sickNoteExtensionService.findSubmittedExtensionOfSickNote(sickNote)
                .ifPresentOrElse(
                    extension -> {
                        model.addAttribute("extensionRequested", true);
                        model.addAttribute("sickNotePreviewCurrent", toSickNoteExtensionPreviewDto(sickNote));
                        model.addAttribute("sickNotePreviewNext", toSickNoteExtensionPreviewDto(sickNote, extension));
                    },
                    () ->
                        model.addAttribute("extensionRequested", false)
                );

            final List<SickNoteCommentEntity> comments = sickNoteCommentService.getCommentsBySickNote(sickNote);
            model.addAttribute("comments", comments);

            model.addAttribute("canAcceptSickNote", isOffice || isAllowedToEditSickNote);
            model.addAttribute("canEditSickNote", isOffice || isAllowedToEditSickNote || (isSamePerson && sickNote.isSubmitted()));
            model.addAttribute("canConvertSickNote", isOffice);
            model.addAttribute("canDeleteSickNote", isOffice || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_CANCEL, sickNotePerson));
            model.addAttribute("canCommentSickNote", isOffice || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_COMMENT, sickNotePerson));

            model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(sickNotePerson));

            model.addAttribute("action", requireNonNullElse(action, ""));
            model.addAttribute("shortcut", shortcut);
            model.addAttribute("redirect", redirect);

            return "sicknote/sick_note_detail";
        }

        throw new AccessDeniedException(format(
            "User '%s' has not the correct permissions to see the sick note of user '%s'",
            signedInUser.getId(), sickNotePerson.getId()));
    }

    @PostMapping("/sicknote/{id}/extension/accept")
    public String acceptSickNoteExtension(@PathVariable("id") Long sickNoteId,
                                          @ModelAttribute("comment") SickNoteCommentFormDto comment,
                                          @RequestParam(value = "redirect", required = false) String redirectUrl) {

        final Person signedInUser = personService.getSignedInUser();
        sickNoteExtensionInteractionService.acceptSubmittedExtension(signedInUser, sickNoteId, comment.getText());

        return redirectToSickNoteDetailOr(redirectUrl, sickNoteId);
    }

    @GetMapping("/sicknote/new")
    public String newSickNote(@RequestParam(value = "person", required = false) Long personId,
                              @RequestParam(value = "noExtensionRedirect", required = false) String noExtensionRedirect,
                              @RequestParam(value = "category", required = false) Optional<SickNoteCategory> category,
                              Model model) throws UnknownPersonException {

        final Person signedInUser = personService.getSignedInUser();
        final boolean userIsAllowedToSubmitSickNotes = settingsService.getSettings().getSickNoteSettings().getUserIsAllowedToSubmitSickNotes();

        if (!signedInUser.hasAnyRole(OFFICE, SICK_NOTE_ADD) && !userIsAllowedToSubmitSickNotes) {
            throw new AccessDeniedException(
                "User '%s' has not the correct permissions to create a sick note".formatted(
                    signedInUser.getId()));
        }

        final Person sickNotePerson = personId == null
            ? signedInUser
            : personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        if (userIsAllowedToSubmitSickNotes) {
            final boolean noRedirect = noExtensionRedirect != null && (noExtensionRedirect.isEmpty() || "true".equalsIgnoreCase(noExtensionRedirect));
            final Optional<SickNote> sickNoteOfYesterdayOrLastWorkDay = sickNoteService.getSickNoteOfYesterdayOrLastWorkDay(sickNotePerson);
            if (!noRedirect && (sickNoteOfYesterdayOrLastWorkDay.isPresent() && sickNoteOfYesterdayOrLastWorkDay.get().getDayLength().isFull())) {
                LOG.info("sick note of last work day found");
                return "redirect:/web/sicknote/extend";
            } else {
                LOG.info("no sick note of last work day found");
            }
        }

        model.addAttribute("signedInUser", signedInUser);
        model.addAttribute("person", sickNotePerson);

        final List<SickNoteType> sickNoteTypes = sickNoteTypeService.getSickNoteTypes();

        final SickNoteFormDto sickNoteFormDto = new SickNoteFormDto();
        category.flatMap(cat -> sickNoteTypes.stream().filter(type -> type.isOfCategory(cat)).findFirst()).ifPresent(sickNoteFormDto::setSickNoteType);

        model.addAttribute("sickNote", sickNoteFormDto);

        final List<Person> managedPersons = getManagedPersons(signedInUser);
        model.addAttribute("persons", managedPersons);
        model.addAttribute("canAddSickNote", canAddSickNote(signedInUser, sickNotePerson));
        model.addAttribute("sickNoteTypes", sickNoteTypes);

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

        final boolean personIsApplier = sickNote.getPerson().equals(sickNote.getApplier());
        final boolean allowedToSubmitSickNotes = settingsService.getSettings().getSickNoteSettings().getUserIsAllowedToSubmitSickNotes();

        final SickNote updatedSickNote;
        if (signedInUser.hasAnyRole(OFFICE, SICK_NOTE_ADD) || (personIsApplier && !allowedToSubmitSickNotes)) {
            updatedSickNote = sickNoteInteractionService.create(sickNote, signedInUser, sickNoteFormDto.getComment());
        } else {
            updatedSickNote = sickNoteInteractionService.submit(sickNote, signedInUser, sickNoteFormDto.getComment());
        }

        return "redirect:/web/sicknote/" + updatedSickNote.getId();
    }

    @GetMapping("/sicknote/{id}/edit")
    public String editSickNote(@PathVariable("id") Long id, Model model) throws UnknownSickNoteException, SickNoteAlreadyInactiveException {

        final SickNote sickNote = getSickNote(id);
        if (!sickNote.isActive()) {
            throw new SickNoteAlreadyInactiveException(id);
        }

        final Person signedInUser = personService.getSignedInUser();
        final Person sickNotePerson = sickNote.getPerson();

        if (!signedInUser.hasRole(OFFICE)
            && !isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_EDIT, sickNotePerson)
            && !(sickNotePerson.equals(signedInUser) && sickNote.isSubmitted())) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to edit the sick note of user '%s'",
                signedInUser.getId(), sickNotePerson.getId()));
        }

        final SickNoteFormDto sickNoteFormDto = toSickNoteForm(sickNote);

        model.addAttribute("sickNote", sickNoteFormDto);
        model.addAttribute("person", sickNotePerson);
        model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());
        model.addAttribute("canAddSickNote", canAddSickNote(signedInUser, sickNotePerson));

        addVacationTypeColorsToModel(model);

        return "sicknote/sick_note_form";
    }

    @PostMapping("/sicknote/{id}/edit")
    public String editSickNote(@PathVariable("id") Long sickNoteId,
                               @ModelAttribute("sickNote") SickNoteFormDto sickNoteFormDto, Errors errors, Model model) throws UnknownSickNoteException {

        final Optional<SickNote> maybeSickNote = sickNoteService.getById(sickNoteId);
        if (maybeSickNote.isEmpty()) {
            throw new UnknownSickNoteException(sickNoteId);
        }

        final SickNote persistedSickNote = maybeSickNote.get();
        final Person signedInUser = personService.getSignedInUser();
        final Person sickNotePerson = persistedSickNote.getPerson();

        if (!signedInUser.hasRole(OFFICE)
            && !isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_EDIT, sickNotePerson)
            && !(sickNotePerson.equals(signedInUser) && persistedSickNote.isSubmitted())) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to edit the sick note of user '%s'",
                signedInUser.getId(), sickNotePerson.getId()));
        }

        final SickNote editedSickNote = merge(persistedSickNote, sickNoteFormDto);
        sickNoteValidator.validate(editedSickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute("errors", errors);
            model.addAttribute("sickNote", sickNoteFormDto);
            model.addAttribute("person", sickNotePerson);
            model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());
            model.addAttribute("canAddSickNote", canAddSickNote(signedInUser, editedSickNote.getPerson()));

            addVacationTypeColorsToModel(model);

            return "sicknote/sick_note_form";
        }

        sickNoteInteractionService.update(editedSickNote, signedInUser, sickNoteFormDto.getComment());

        return "redirect:/web/sicknote/" + sickNoteId;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_EDIT')")
    @PostMapping("/sicknote/{id}/accept")
    public String acceptSickNote(@PathVariable("id") Long sickNoteId,
                                 @ModelAttribute("comment") SickNoteCommentFormDto comment, Errors errors,
                                 @RequestParam(value = "redirect", required = false) String redirectUrl,
                                 RedirectAttributes redirectAttributes
    ) throws UnknownSickNoteException {

        final Optional<SickNote> maybeSickNote = sickNoteService.getById(sickNoteId);
        if (maybeSickNote.isEmpty()) {
            throw new UnknownSickNoteException(sickNoteId);
        }

        comment.setMandatory(false);
        sickNoteCommentFormValidator.validate(comment, errors);
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/web/sicknote/" + sickNoteId + "?action=allow&redirect=%s".formatted(requireNonNullElse(redirectUrl, ""));
        }

        final Person signedInUser = personService.getSignedInUser();
        final SickNote acceptedSickNote = sickNoteInteractionService.accept(maybeSickNote.get(), signedInUser, comment.getText());

        if (SickNoteStatus.ACTIVE.equals(acceptedSickNote.getStatus())) {
            redirectAttributes.addFlashAttribute("acceptSickNoteSuccess", true);
        }

        return redirectToSickNoteDetailOr(redirectUrl, sickNoteId);
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
        if (!sickNote.isActive()) {
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
    public String cancelSickNote(@PathVariable("id") Long id,
                                 @ModelAttribute("comment") SickNoteCommentFormDto comment,
                                 Errors errors,
                                 @RequestParam(value = "redirect", required = false) String redirectUrl,
                                 RedirectAttributes redirectAttributes) throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        final Person signedInUser = personService.getSignedInUser();

        if (!signedInUser.hasRole(OFFICE) && !isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_CANCEL, sickNote.getPerson())) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to cancel the sick note of user '%s'",
                signedInUser.getId(), sickNote.getPerson().getId()));
        }

        comment.setMandatory(true);
        sickNoteCommentFormValidator.validate(comment, errors);
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/web/sicknote/" + id + "?action=cancel&redirect=%s".formatted(requireNonNullElse(redirectUrl, ""));
        }

        final SickNote cancelledSickNote = sickNoteInteractionService.cancel(sickNote, signedInUser, comment.getText());

        if (SickNoteStatus.CANCELLED.equals(cancelledSickNote.getStatus())) {
            redirectAttributes.addFlashAttribute("cancelSickNoteSuccess", true);
        }

        if (redirectUrl != null && redirectUrl.equals("/web/sicknote/submitted")) {
            return "redirect:" + redirectUrl;
        }

        return "redirect:/web/sicknote/" + cancelledSickNote.getId();
    }

    private String redirectToSickNoteDetailOr(String redirectUrl, Long sickNoteId) {
        if ("/web/sicknote/submitted".equals(redirectUrl)) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/web/sicknote/" + sickNoteId;
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
            .toList();
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

    private static SickNote merge(SickNote sickNote, SickNoteFormDto sickNoteFormDto) {
        return SickNote.builder(sickNote)
            .person(sickNoteFormDto.getPerson())
            .sickNoteType(sickNoteFormDto.getSickNoteType())
            .startDate(sickNoteFormDto.getStartDate())
            .endDate(sickNoteFormDto.getEndDate())
            .dayLength(sickNoteFormDto.getDayLength())
            .aubStartDate(sickNoteFormDto.getAubStartDate())
            .aubEndDate(sickNoteFormDto.getAubEndDate())
            .build();
    }

    private SickNoteExtendPreviewDto toSickNoteExtensionPreviewDto(SickNote sickNote) {
        return new SickNoteExtendPreviewDto(sickNote.getStartDate(), sickNote.getEndDate(), sickNote.getWorkDays());
    }

    private SickNoteExtendPreviewDto toSickNoteExtensionPreviewDto(SickNote sickNote, SickNoteExtension extension) {
        final BigDecimal workingDays = sickNote.getWorkDays().add(extension.additionalWorkdays());
        return new SickNoteExtendPreviewDto(sickNote.getStartDate(), extension.nextEndDate(), workingDays);
    }
}
