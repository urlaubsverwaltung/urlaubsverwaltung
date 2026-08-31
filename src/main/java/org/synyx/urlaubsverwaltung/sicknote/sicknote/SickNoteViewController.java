package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.slf4j.Logger;
import org.jspecify.annotations.Nullable;
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
import org.synyx.urlaubsverwaltung.web.SafeRedirectUrl;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Comparator.comparing;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNullElse;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web")
class SickNoteViewController implements HasLaunchpad, HasPersonSearch {

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
    private final SickNotePermissionEvaluator sickNotePermissionEvaluator;
    private final SickNoteValidator sickNoteValidator;
    private final SickNoteCommentFormValidator sickNoteCommentFormValidator;
    private final SickNoteConvertFormValidator sickNoteConvertFormValidator;
    private final SettingsService settingsService;
    private final PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    private final PersonSearchUiFragmentSupplier personSearchTemplateSupplier;
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
        SickNotePermissionEvaluator sickNotePermissionEvaluator,
        SickNoteValidator sickNoteValidator,
        SickNoteCommentFormValidator sickNoteCommentFormValidator,
        SickNoteConvertFormValidator sickNoteConvertFormValidator,
        SettingsService settingsService,
        PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy,
        PersonSearchUiFragmentSupplier personSearchTemplateSupplier,
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
        this.sickNotePermissionEvaluator = sickNotePermissionEvaluator;
        this.sickNoteValidator = sickNoteValidator;
        this.sickNoteCommentFormValidator = sickNoteCommentFormValidator;
        this.sickNoteConvertFormValidator = sickNoteConvertFormValidator;
        this.settingsService = settingsService;
        this.defaultPersonSuggestionUrlStrategy = defaultPersonSuggestionUrlStrategy;
        this.personSearchTemplateSupplier = personSearchTemplateSupplier;
        this.clock = clock;
    }

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }

    @Override
    public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
        return (suggestion, context) -> {
            if (context.getRequestPath().equals("/web/sicknote/new")) {
                return "/web/sicknote/new?person=" + suggestion.getId();
            }
            // detail / edit / convert page -> link to overview
            return defaultPersonSuggestionUrlStrategy.buildSuggestionMainLink(suggestion, context);
        };
    }

    @Override
    public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
        return personSearchTemplateSupplier;
    }

    @GetMapping("/sicknote/{id}")
    public String sickNoteDetails(
        @PathVariable("id") Long id,
        @RequestParam(value = "action", required = false) String action,
        @RequestParam(value = "redirect", required = false) String redirect,
        Model model
    ) throws UnknownSickNoteException {

        final Person signedInUser = personService.getSignedInUser();
        final SickNote sickNote = getSickNote(id);
        final Person sickNotePerson = sickNote.getPerson();

        final SickNotePermissions permissions = sickNotePermissionEvaluator.of(signedInUser, sickNotePerson);

        if (permissions.isAllowedToView()) {

            model.addAttribute("sickNote", sickNote);
            model.addAttribute("comment", new SickNoteCommentFormDto());

            final Integer maximumSickPayDays = settingsService.getSettings().getSickNoteSettings().getMaximumSickPayDays();
            final LocalDate sickPayDaysEndDate = sickNote.getStartDate().plusDays(maximumSickPayDays).minusDays(1);
            model.addAttribute("sickPayDaysEndDate", sickPayDaysEndDate);
            model.addAttribute("doesSickPayDaysEnd", !sickNote.getEndDate().isBefore(sickNote.getStartDate().plusDays(maximumSickPayDays)));
            model.addAttribute("numberSickPayDaysSinceEnd", ChronoUnit.DAYS.between(sickPayDaysEndDate, LocalDate.now(clock)) + 1);

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

            model.addAttribute("canAcceptSickNote", permissions.isAllowedToAccept());
            model.addAttribute("canEditSickNote", permissions.isAllowedToEdit(sickNote));
            model.addAttribute("canConvertSickNote", permissions.isAllowedToConvert());
            model.addAttribute("canDeleteSickNote", permissions.isAllowedToCancel());
            model.addAttribute("canCommentSickNote", permissions.isAllowedToComment());

            model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(sickNotePerson));

            model.addAttribute("action", requireNonNullElse(action, ""));
            model.addAttribute("redirect", redirect);

            return "sicknote/sick_note_detail";
        }

        throw new AccessDeniedException("User '%s' has not the correct permissions to see the sick note of user '%s'".formatted(
            signedInUser.getId(), sickNotePerson.getId()));
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_EDIT')")
    @PostMapping("/sicknote/{id}/extension/accept")
    public String acceptSickNoteExtension(
        @PathVariable("id") Long sickNoteId,
        @ModelAttribute("comment") SickNoteCommentFormDto comment,
        @RequestParam(value = "redirect", required = false) String redirectUrl
    ) throws UnknownSickNoteException {

        final Person signedInUser = personService.getSignedInUser();
        final SickNote sickNote = getSickNote(sickNoteId);

        if (!sickNotePermissionEvaluator.of(signedInUser, sickNote).isAllowedToAcceptExtension()) {
            throw new AccessDeniedException("User '%s' has not the correct permissions to accept a submitted sick note extension for %s".formatted(signedInUser.getId(), sickNote.getPerson()));
        }

        sickNoteExtensionInteractionService.acceptSubmittedExtension(signedInUser, sickNoteId, comment.getText());

        return redirectToSickNoteDetailOr(redirectUrl, sickNoteId);
    }

    @GetMapping("/sicknote/new")
    public String newSickNote(
        @RequestParam(value = "person", required = false) Long personId,
        @RequestParam(value = "noExtensionRedirect", required = false) String noExtensionRedirect,
        @RequestParam(value = "category", required = false) Optional<SickNoteCategory> category,
        Model model
    ) throws UnknownPersonException {

        final Person signedInUser = personService.getSignedInUser();
        final boolean userIsAllowedToSubmitSickNotes = isUserAllowedToSubmitSickNotes();

        final Person sickNotePerson = personId == null
            ? signedInUser
            : personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        final SickNotePermissions permissions = sickNotePermissionEvaluator.of(signedInUser, sickNotePerson);
        // the form itself is accessible for everyone who may create a sick note for at least one person, the person to
        // create it for is chosen within the form and checked when the form is submitted.
        final boolean canAddSickNoteForAnotherPerson = sickNotePermissionEvaluator.isAllowedToAddSickNotesForOtherPersons(signedInUser);

        if (!canAddSickNoteForAnotherPerson && !permissions.isAllowedToAdd() && !permissions.isAllowedToSubmit()) {
            throw new AccessDeniedException("User '%s' has not the correct permissions to create a sick note".formatted(signedInUser.getId()));
        }

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
        model.addAttribute("canAddSickNoteForAnotherPerson", canAddSickNoteForAnotherPerson);
        model.addAttribute("sickNoteTypes", sickNoteTypes);

        addVacationTypeColorsToModel(model);

        return "sicknote/sick_note_form";
    }

    @PostMapping("/sicknote")
    public String addOrSubmitNewSickNote(@ModelAttribute("sickNote") SickNoteFormDto sickNoteFormDto, Errors errors, Model model) {

        final Person signedInUser = personService.getSignedInUser();
        final Person sickNotePerson = sickNoteFormDto.getPerson();
        final SickNotePermissions permissions = sickNotePermissionEvaluator.of(signedInUser, sickNotePerson);

        if (!permissions.isAllowedToAdd() && !permissions.isAllowedToSubmit()) {
            throw new AccessDeniedException("User '%s' has not the correct permissions to create a sick note".formatted(signedInUser.getId()));
        }

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
            model.addAttribute("signedInUser", signedInUser);
            model.addAttribute("errors", errors);
            model.addAttribute("sickNote", sickNoteFormDto);
            model.addAttribute("person", sickNotePerson);
            model.addAttribute("persons", getManagedPersons(signedInUser));
            model.addAttribute("canAddSickNoteForAnotherPerson", sickNotePermissionEvaluator.isAllowedToAddSickNotesForOtherPersons(signedInUser));
            model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());

            addVacationTypeColorsToModel(model);

            return "sicknote/sick_note_form";
        }

        final SickNote updatedSickNote = permissions.isAllowedToAdd()
            ? sickNoteInteractionService.create(sickNote, signedInUser, sickNoteFormDto.getComment())
            : sickNoteInteractionService.submit(sickNote, signedInUser, sickNoteFormDto.getComment());

        return "redirect:/web/sicknote/" + updatedSickNote.getId();
    }

    @GetMapping("/sicknote/{id}/edit")
    public String editSickNote(@PathVariable("id") Long id, Model model) throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        if (!sickNote.isActive()) {
            return "redirect:/web/sicknote/" + id;
        }

        final Person signedInUser = personService.getSignedInUser();
        final Person sickNotePerson = sickNote.getPerson();
        final SickNotePermissions permissions = sickNotePermissionEvaluator.of(signedInUser, sickNotePerson);

        if (!permissions.isAllowedToEdit(sickNote)) {
            throw new AccessDeniedException("User '%s' has not the correct permissions to edit the sick note of user '%s'".formatted(
                signedInUser.getId(), sickNotePerson.getId()));
        }

        final SickNoteFormDto sickNoteFormDto = toSickNoteForm(sickNote);

        model.addAttribute("sickNote", sickNoteFormDto);
        model.addAttribute("person", sickNotePerson);
        model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());
        model.addAttribute("canAddSickNoteForAnotherPerson", sickNotePermissionEvaluator.isAllowedToAddSickNotesForOtherPersons(signedInUser));

        addVacationTypeColorsToModel(model);

        return "sicknote/sick_note_form";
    }

    @PostMapping("/sicknote/{id}/edit")
    public String editSickNote(
        @PathVariable("id") Long sickNoteId,
        @ModelAttribute("sickNote") SickNoteFormDto sickNoteFormDto, Errors errors, Model model
    ) throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(sickNoteId);
        final Person signedInUser = personService.getSignedInUser();
        final Person sickNotePerson = sickNote.getPerson();
        final SickNotePermissions permissions = sickNotePermissionEvaluator.of(signedInUser, sickNotePerson);

        if (!permissions.isAllowedToEdit(sickNote)) {
            throw new AccessDeniedException("User '%s' has not the correct permissions to edit the sick note of user '%s'".formatted(
                signedInUser.getId(), sickNotePerson.getId()));
        }

        final SickNote editedSickNote = merge(sickNote, sickNoteFormDto);
        sickNoteValidator.validate(editedSickNote, errors);

        if (errors.hasErrors()) {
            model.addAttribute("errors", errors);
            model.addAttribute("sickNote", sickNoteFormDto);
            model.addAttribute("person", sickNotePerson);
            model.addAttribute("sickNoteTypes", sickNoteTypeService.getSickNoteTypes());
            model.addAttribute("canAddSickNoteForAnotherPerson", sickNotePermissionEvaluator.isAllowedToAddSickNotesForOtherPersons(signedInUser));

            addVacationTypeColorsToModel(model);

            return "sicknote/sick_note_form";
        }

        sickNoteInteractionService.update(editedSickNote, signedInUser, sickNoteFormDto.getComment());

        return "redirect:/web/sicknote/" + sickNoteId;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_EDIT')")
    @PostMapping("/sicknote/{id}/accept")
    public String acceptSickNote(
        @PathVariable("id") Long sickNoteId,
        @ModelAttribute("comment") SickNoteCommentFormDto comment, Errors errors,
        @RequestParam(value = "redirect", required = false) String redirectUrl,
        RedirectAttributes redirectAttributes
    ) throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(sickNoteId);
        final Person signedInUser = personService.getSignedInUser();

        if (!sickNotePermissionEvaluator.of(signedInUser, sickNote).isAllowedToAccept()) {
            throw new AccessDeniedException("User '%s' has not the correct permissions to accept the sick note of user '%s'".formatted(signedInUser.getId(), sickNote.getPerson().getId()));
        }

        comment.setMandatory(false);
        sickNoteCommentFormValidator.validate(comment, errors);
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/web/sicknote/" + sickNoteId + "?action=allow&redirect=%s".formatted(encode(safeRedirectUrlOrEmpty(redirectUrl), UTF_8));
        }

        final SickNote acceptedSickNote = sickNoteInteractionService.accept(sickNote, signedInUser, comment.getText());


        if (SickNoteStatus.ACTIVE.equals(acceptedSickNote.getStatus())) {
            redirectAttributes.addFlashAttribute("acceptSickNoteSuccess", true);
        }

        return redirectToSickNoteDetailOr(redirectUrl, sickNoteId);
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_COMMENT')")
    @PostMapping("/sicknote/{id}/comment")
    public String addComment(
        @PathVariable("id") Long id,
        @ModelAttribute("comment") SickNoteCommentFormDto comment, Errors errors, RedirectAttributes redirectAttributes
    ) throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        final Person signedInUser = personService.getSignedInUser();

        if (!sickNotePermissionEvaluator.of(signedInUser, sickNote).isAllowedToComment()) {
            throw new AccessDeniedException("User '%s' has not the correct permissions to comment the sick note of user '%s'".formatted(
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
    public String convertSickNoteToVacation(@PathVariable("id") Long id, Model model) throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        if (!sickNote.isActive()) {
            return "redirect:/web/sicknote/" + id;
        }

        model.addAttribute("sickNote", sickNote);
        model.addAttribute("sickNoteConvertForm", new SickNoteConvertForm(sickNote));
        model.addAttribute("vacationTypes", getActiveVacationTypes());

        return "sicknote/sick_note_convert";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/sicknote/{id}/convert")
    public String convertSickNoteToVacation(
        @PathVariable("id") Long id,
        @ModelAttribute("sickNoteConvertForm") SickNoteConvertForm sickNoteConvertForm,
        Errors errors, Model model
    ) throws UnknownSickNoteException {

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
    public String cancelSickNote(
        @PathVariable("id") Long id,
        @ModelAttribute("comment") SickNoteCommentFormDto comment,
        Errors errors,
        @RequestParam(value = "redirect", required = false) String redirectUrl,
        RedirectAttributes redirectAttributes
    ) throws UnknownSickNoteException {

        final SickNote sickNote = getSickNote(id);
        final Person signedInUser = personService.getSignedInUser();

        if (!sickNotePermissionEvaluator.of(signedInUser, sickNote).isAllowedToCancel()) {
            throw new AccessDeniedException("User '%s' has not the correct permissions to cancel the sick note of user '%s'".formatted(signedInUser.getId(), sickNote.getPerson().getId()));
        }

        comment.setMandatory(true);
        sickNoteCommentFormValidator.validate(comment, errors);
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/web/sicknote/" + id + "?action=cancel&redirect=%s".formatted(encode(safeRedirectUrlOrEmpty(redirectUrl), UTF_8));
        }

        final SickNote cancelledSickNote = sickNoteInteractionService.cancel(sickNote, signedInUser, comment.getText());

        if (SickNoteStatus.CANCELLED.equals(cancelledSickNote.getStatus())) {
            redirectAttributes.addFlashAttribute("cancelSickNoteSuccess", true);
        }

        return redirectToSickNoteDetailOr(redirectUrl, cancelledSickNote.getId());
    }

    /**
     * Redirects to the page the action was started from, falling back to the detail page of the sick note when no
     * such page was handed over - or when it would leave this application, see {@link SafeRedirectUrl}.
     *
     * @param redirectUrl page the action was started from, may be {@code null}
     * @param sickNoteId  sick note to fall back to
     * @return the view name to redirect to
     */
    private String redirectToSickNoteDetailOr(@Nullable String redirectUrl, Long sickNoteId) {
        return SafeRedirectUrl.ofKnownOrigin(redirectUrl)
            .map("redirect:"::concat)
            .orElse("redirect:/web/sicknote/" + sickNoteId);
    }

    private static String safeRedirectUrlOrEmpty(@Nullable String redirectUrl) {
        return SafeRedirectUrl.ofKnownOrigin(redirectUrl).orElse("");
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

        if (vacationType.isOfCategory(OVERTIME)) {
            applicationForLeave.setHours(Duration.ZERO);
        }

        applicationForLeave.setApplicationDate(LocalDate.now(clock));
        applicationForLeave.setEditedDate(LocalDate.now(clock));

        return applicationForLeave;
    }

    private static SickNote merge(SickNote sickNote, SickNoteFormDto sickNoteFormDto) {
        // the person of a sick note cannot be changed - taking it from the form would allow to move a sick note to a
        // person the editing user is not allowed to manage, since the permission is checked on the persisted person.
        return SickNote.builder(sickNote)
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

    private boolean isUserAllowedToSubmitSickNotes() {
        return settingsService.getSettings().getSickNoteSettings().getUserIsAllowedToSubmitSickNotes();
    }
}
