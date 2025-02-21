package org.synyx.urlaubsverwaltung.application.application;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.TimePropertyEditor;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static java.util.stream.Stream.concat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToEditApplication;
import static org.synyx.urlaubsverwaltung.application.application.SpecialLeaveDtoMapper.mapToSpecialLeaveSettingsDto;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * Controller to apply for leave.
 */
@Controller
@RequestMapping("/web")
class ApplicationForLeaveFormViewController implements HasLaunchpad {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final String NO_HOLIDAYS_ACCOUNT = "noHolidaysAccount";
    private static final String USER_HAS_NOT_THE_CORRECT_PERMISSIONS = "User '%s' has not the correct permissions to apply for leave for user '%s'";

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final AccountService accountService;
    private final VacationTypeService vacationTypeService;
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final ApplicationInteractionService applicationInteractionService;
    private final ApplicationForLeaveFormValidator applicationForLeaveFormValidator;
    private final SettingsService settingsService;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;
    private final SpecialLeaveSettingsService specialLeaveSettingsService;
    private final ApplicationMapper applicationMapper;

    @Autowired
    ApplicationForLeaveFormViewController(
        PersonService personService, DepartmentService departmentService, AccountService accountService,
        VacationTypeService vacationTypeService,
        VacationTypeViewModelService vacationTypeViewModelService, ApplicationInteractionService applicationInteractionService,
        ApplicationForLeaveFormValidator applicationForLeaveFormValidator,
        SettingsService settingsService, DateFormatAware dateFormatAware,
        Clock clock, SpecialLeaveSettingsService specialLeaveSettingsService,
        ApplicationMapper applicationMapper
    ) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.accountService = accountService;
        this.vacationTypeService = vacationTypeService;
        this.vacationTypeViewModelService = vacationTypeViewModelService;
        this.applicationInteractionService = applicationInteractionService;
        this.applicationForLeaveFormValidator = applicationForLeaveFormValidator;
        this.settingsService = settingsService;
        this.dateFormatAware = dateFormatAware;
        this.clock = clock;
        this.specialLeaveSettingsService = specialLeaveSettingsService;
        this.applicationMapper = applicationMapper;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {
        binder.registerCustomEditor(Time.class, new TimePropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }

    @GetMapping("/application/new")
    public String newApplicationForm(
        @RequestParam(value = "personId", required = false) Long personId,
        @RequestParam(value = "from", required = false) String startDateString,
        @RequestParam(value = "to", required = false) String endDateString,
        Model model, Locale locale
    ) {

        final Person signedInUser = personService.getSignedInUser();
        final Person person = getPersonByRequestParam(personId).orElse(signedInUser);

        final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(ZonedDateTime.now(clock).getYear(), person);
        if (holidaysAccount.isPresent()) {

            final LocalDate startDate = dateFormatAware.parse(startDateString, locale).orElse(null);
            final LocalDate endDate = dateFormatAware.parse(endDateString, locale).orElse(startDate);

            final ApplicationForLeaveForm appForLeaveForm = new ApplicationForLeaveForm();
            appForLeaveForm.setStartDate(startDate);
            appForLeaveForm.setEndDate(endDate);

            prepareApplicationForLeaveForm(signedInUser, person, appForLeaveForm, model, locale);
            addSelectableHolidayReplacementsToModel(model, selectableHolidayReplacements(not(isEqual(person))));
        }

        model.addAttribute(NO_HOLIDAYS_ACCOUNT, holidaysAccount.isEmpty());
        return "application/application_form";
    }

    @PostMapping(value = {"/application/new", "/application/{applicationId}/edit"}, params = "add-holiday-replacement")
    public String addHolidayReplacement(ApplicationForLeaveForm applicationForLeaveForm, Model model, Locale locale) {

        final Person signedInUser = personService.getSignedInUser();
        final Person person = ofNullable(applicationForLeaveForm.getPerson())
            .orElse(signedInUser);

        if (!isPersonAllowedToExecuteRoleOn(signedInUser, APPLICATION_ADD, person)) {
            throw new AccessDeniedException(format(USER_HAS_NOT_THE_CORRECT_PERMISSIONS, signedInUser.getId(), person.getId()));
        }

        final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(ZonedDateTime.now(clock).getYear(), person);
        if (holidaysAccount.isPresent()) {
            final Person replacementPersonToAdd = applicationForLeaveForm.getHolidayReplacementToAdd();
            if (replacementPersonToAdd == null) {
                final List<SelectableHolidayReplacementDto> selectableHolidayReplacementDtos = selectableHolidayReplacements(
                    not(containsPerson(holidayReplacementPersonsOfApplication(applicationForLeaveForm)))
                        .and(not(isEqual(person)))
                );
                addSelectableHolidayReplacementsToModel(model, selectableHolidayReplacementDtos);
            } else {
                // add replacementToAdd to the replacements list
                final HolidayReplacementDto replacementDto = new HolidayReplacementDto();
                replacementDto.setPerson(replacementPersonToAdd);
                applicationForLeaveForm.getHolidayReplacements().add(replacementDto);
                // reset holidayReplacement selection element
                applicationForLeaveForm.setHolidayReplacementToAdd(null);

                // and remove it from the selectable elements
                final List<SelectableHolidayReplacementDto> nextSelectableReplacements = selectableHolidayReplacements(
                    not(
                        personEquals(replacementPersonToAdd)
                            .or(containsPerson(holidayReplacementPersonsOfApplication(applicationForLeaveForm)))
                    ).and(not(isEqual(person)))
                );
                addSelectableHolidayReplacementsToModel(model, nextSelectableReplacements);
            }

            prepareApplicationForLeaveForm(signedInUser, person, applicationForLeaveForm, model, locale);
        }

        model.addAttribute(NO_HOLIDAYS_ACCOUNT, holidaysAccount.isEmpty());

        return "application/application_form";
    }

    @PostMapping(value = {"/application/new/replacements", "/application/{applicationId}/replacements"}, headers = {"X-Requested-With=ajax"})
    public String ajaxAddHolidayReplacement(@ModelAttribute("applicationForLeaveForm") ApplicationForLeaveForm applicationForLeave, Model model) {

        final Person signedInUser = personService.getSignedInUser();
        final Person person = ofNullable(applicationForLeave.getPerson()).orElse(signedInUser);

        if (!isPersonAllowedToExecuteRoleOn(signedInUser, APPLICATION_ADD, person)) {
            throw new AccessDeniedException(format(USER_HAS_NOT_THE_CORRECT_PERMISSIONS, signedInUser.getId(), person.getId()));
        }

        final Person replacementPersonToAdd = applicationForLeave.getHolidayReplacementToAdd();
        if (replacementPersonToAdd != null) {
            final List<Department> departments = departmentService.getAssignedDepartmentsOfMember(replacementPersonToAdd);

            final HolidayReplacementDto replacementDto = new HolidayReplacementDto();
            replacementDto.setPerson(replacementPersonToAdd);
            replacementDto.setDepartments(departments.stream().map(Department::getName).toList());

            model.addAttribute("holidayReplacement", replacementDto);
        }

        model.addAttribute("index", applicationForLeave.getHolidayReplacements().size());

        final Long applicationForLeaveId = applicationForLeave.getId();
        if (applicationForLeaveId == null) {
            model.addAttribute("deleteButtonFormActionValue", "/web/application/new");
        } else {
            model.addAttribute("deleteButtonFormActionValue", "/web/application/" + applicationForLeaveId + "/edit");
        }

        return "application/application-form :: replacement-item";
    }

    @PostMapping(value = {"/application/new", "/application/{applicationId}/edit"}, params = "remove-holiday-replacement")
    public String removeHolidayReplacement(@ModelAttribute("applicationForLeaveForm") ApplicationForLeaveForm applicationForLeaveForm,
                                           @RequestParam(name = "remove-holiday-replacement") Long personIdToRemove,
                                           Model model, Locale locale) {

        final Person signedInUser = personService.getSignedInUser();
        final Person person = ofNullable(applicationForLeaveForm.getPerson()).orElse(signedInUser);

        if (!isPersonAllowedToExecuteRoleOn(signedInUser, APPLICATION_ADD, person)) {
            throw new AccessDeniedException(format(USER_HAS_NOT_THE_CORRECT_PERMISSIONS, signedInUser.getId(), person.getId()));
        }

        final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(ZonedDateTime.now(clock).getYear(), person);
        if (holidaysAccount.isPresent()) {
            final List<HolidayReplacementDto> newList = applicationForLeaveForm.getHolidayReplacements()
                .stream()
                .filter(holidayReplacementDto -> !holidayReplacementDto.getPerson().getId().equals(personIdToRemove))
                .toList();
            applicationForLeaveForm.setHolidayReplacements(newList);
            prepareApplicationForLeaveForm(signedInUser, person, applicationForLeaveForm, model, locale);

            final List<SelectableHolidayReplacementDto> selectableHolidayReplacements = selectableHolidayReplacements(
                personEquals(personIdToRemove)
                    .or(not(containsPerson(holidayReplacementPersonsOfApplication(applicationForLeaveForm))))
                    .and(not(isEqual(person)))
            );
            addSelectableHolidayReplacementsToModel(model, selectableHolidayReplacements);
        }

        model.addAttribute(NO_HOLIDAYS_ACCOUNT, holidaysAccount.isEmpty());

        return "application/application_form";
    }

    @PostMapping("/application")
    public String newApplication(@ModelAttribute("applicationForLeaveForm") ApplicationForLeaveForm appForm, Errors errors,
                                 Model model, Locale locale, RedirectAttributes redirectAttributes) {
        LOG.info("POST new application received: {}", appForm);

        final Person applier = personService.getSignedInUser();
        final Person person = ofNullable(appForm.getPerson()).orElse(applier);

        if (!isPersonAllowedToExecuteRoleOn(applier, APPLICATION_ADD, person)) {
            throw new AccessDeniedException(format(USER_HAS_NOT_THE_CORRECT_PERMISSIONS, applier.getId(), person.getId()));
        }

        applicationForLeaveFormValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            final List<SelectableHolidayReplacementDto> selectableHolidayReplacementDtos = selectableHolidayReplacements(
                not(containsPerson(holidayReplacementPersonsOfApplication(appForm)))
                    .and(not(isEqual(person)))
            );
            addSelectableHolidayReplacementsToModel(model, selectableHolidayReplacementDtos);

            prepareApplicationForLeaveForm(applier, appForm.getPerson(), appForm, model, locale);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            LOG.info("new application ({}) has errors: {}", appForm, errors);
            return "application/application_form";
        }

        final Application app = applicationMapper.mapToApplication(appForm);

        final Application savedApplicationForLeave;
        if (app.getVacationType().isRequiresApprovalToApply()) {
            savedApplicationForLeave = applicationInteractionService.apply(app, applier, ofNullable(appForm.getComment()));
        } else {
            savedApplicationForLeave = applicationInteractionService.directAllow(app, applier, ofNullable(appForm.getComment()));
        }
        LOG.info("new application has been saved {}", savedApplicationForLeave);

        redirectAttributes.addFlashAttribute("applySuccess", true);

        return "redirect:/web/application/" + savedApplicationForLeave.getId();
    }

    @GetMapping("/application/{applicationId}/edit")
    public String editApplicationForm(@PathVariable("applicationId") Long applicationId, Model model, Locale locale) {

        final Optional<Application> maybeApplication = applicationInteractionService.get(applicationId);
        if (maybeApplication.isEmpty()) {
            return "application/application-not-editable";
        }

        final Person signedInUser = personService.getSignedInUser();
        final Application application = maybeApplication.get();
        if (!isAllowedToEditApplication(application, signedInUser)) {
            return "application/application-not-editable";
        }

        final ApplicationForLeaveForm applicationForLeaveForm = mapToApplicationForm(application, locale);
        final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(Year.now(clock).getValue(), signedInUser);
        if (holidaysAccount.isPresent()) {
            prepareApplicationForLeaveForm(signedInUser, signedInUser, applicationForLeaveForm, model, locale);

            final List<SelectableHolidayReplacementDto> selectableHolidayReplacements = selectableHolidayReplacements(
                not(containsPerson(holidayReplacementPersonsOfApplication(applicationForLeaveForm)))
                    .and(not(isEqual(signedInUser)))
            );
            model.addAttribute("selectableHolidayReplacements", selectableHolidayReplacements);
        }

        model.addAttribute(NO_HOLIDAYS_ACCOUNT, holidaysAccount.isEmpty());
        model.addAttribute("applicationForLeaveForm", applicationForLeaveForm);

        return "application/application_form";
    }

    @PostMapping("/application/{applicationId}/edit")
    public String sendEditApplicationForm(@PathVariable("applicationId") Long applicationId,
                                          @ModelAttribute("applicationForLeaveForm") ApplicationForLeaveForm appForm, Errors errors,
                                          Model model, Locale locale, RedirectAttributes redirectAttributes) throws UnknownApplicationForLeaveException {

        final Optional<Application> maybeApplication = applicationInteractionService.get(applicationId);
        if (maybeApplication.isEmpty()) {
            throw new UnknownApplicationForLeaveException(applicationId);
        }

        final Application application = maybeApplication.get();
        final Person signedInUser = personService.getSignedInUser();
        if (!isAllowedToEditApplication(application, signedInUser)) {
            redirectAttributes.addFlashAttribute("editError", true);
            return "redirect:/web/application/" + applicationId;
        }

        appForm.setId(application.getId());
        applicationForLeaveFormValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareApplicationForLeaveForm(signedInUser, appForm.getPerson(), appForm, model, locale);
            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            addSelectableHolidayReplacementsToModel(model, selectableHolidayReplacements(
                not(containsPerson(holidayReplacementPersonsOfApplication(appForm)))
                    .and(not(isEqual(signedInUser))))
            );

            LOG.debug("edit application ({}) has errors: {}", appForm, errors);
            return "application/application_form";
        }

        final Application editedApplication = applicationMapper.merge(application, appForm);
        final Application savedApplicationForLeave;
        try {
            savedApplicationForLeave = applicationInteractionService.edit(application, editedApplication, signedInUser, Optional.ofNullable(appForm.getComment()));
        } catch (EditApplicationForLeaveNotAllowedException e) {
            return "application/application-not-editable";
        }

        LOG.debug("Edited application with success applied {}", savedApplicationForLeave);

        redirectAttributes.addFlashAttribute("editSuccess", true);

        return "redirect:/web/application/" + savedApplicationForLeave.getId();
    }

    private Optional<Person> getPersonByRequestParam(Long personId) {
        if (personId == null) {
            return Optional.empty();
        }
        return personService.getPersonByID(personId);
    }

    private void prepareApplicationForLeaveForm(Person signedInUser, Person person, ApplicationForLeaveForm appForm, Model model, Locale locale) {

        final Settings settings = settingsService.getSettings();

        model.addAttribute("person", person);
        final List<Person> managedPersons = getManagedPersons(signedInUser);
        model.addAttribute("persons", managedPersons);
        model.addAttribute("canAddApplicationForLeaveForAnotherUser", !(managedPersons.size() == 1 && managedPersons.contains(signedInUser)));

        final boolean overtimeActive = settings.getOvertimeSettings().isOvertimeActive();
        model.addAttribute("overtimeActive", overtimeActive);

        final List<VacationType<?>> activeVacationTypes = overtimeActive
            ? vacationTypeService.getActiveVacationTypes()
            : vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME);

        final List<ApplicationForLeaveFormVacationTypeDto> activeVacationTypesDtos = activeVacationTypes.stream()
            .map(vacationType -> toApplicationForLeaveFormVacationTypeDto(vacationType, locale))
            .toList();
        model.addAttribute("vacationTypes", activeVacationTypesDtos);

        if (appForm.getVacationType() != null) {
            final VacationType<?> vacationType = findVacationType(activeVacationTypes, appForm.getVacationType().getId());
            appForm.setVacationType(toApplicationForLeaveFormVacationTypeDto(vacationType, locale));
        }

        final List<SpecialLeaveSettingsItem> specialLeaveSettings = specialLeaveSettingsService.getSpecialLeaveSettings().stream()
            .filter(SpecialLeaveSettingsItem::active)
            .toList();
        model.addAttribute("specialLeave", mapToSpecialLeaveSettingsDto(specialLeaveSettings));

        appendDepartmentsToReplacements(appForm);
        model.addAttribute("applicationForLeaveForm", appForm);

        final boolean isHalfDayApplication = ofNullable(appForm.getDayLength()).filter(DayLength::isHalfDay).isPresent();
        final boolean isHalfDaysActivated = settings.getApplicationSettings().isAllowHalfDays();
        model.addAttribute("showHalfDayOption", isHalfDayApplication || isHalfDaysActivated);

        final List<VacationTypeDto> vacationTypeColors = vacationTypeViewModelService.getVacationTypeColors();
        model.addAttribute("vacationTypeColors", vacationTypeColors);
    }

    private VacationType<?> findVacationType(Collection<VacationType<?>> vacationTypes, Long id) {
        return vacationTypes.stream()
            .filter(type -> type.getId().equals(id))
            .findFirst()
            .orElseGet(() -> getVacationType(id));
    }

    private VacationType<?> getVacationType(Long id) {
        return vacationTypeService.getById(id).orElseThrow(() -> new IllegalStateException("could not find vacationType with id=" + id));
    }

    private static Predicate<Person> personEquals(Person person) {
        return person::equals;
    }

    private static Predicate<Person> personEquals(Long personId) {
        return p -> p.getId().equals(personId);
    }

    private static Predicate<Person> containsPerson(List<Person> personList) {
        return personList::contains;
    }

    private List<Person> getAllSelectableReplacementPersons() {
        return personService.getActivePersons();
    }

    private List<SelectableHolidayReplacementDto> selectableHolidayReplacements(Predicate<Person> predicate) {
        return getAllSelectableReplacementPersons().stream()
            .filter(predicate)
            .map(ApplicationForLeaveFormViewController::toSelectableHolidayReplacementDto)
            .toList();
    }

    private static void addSelectableHolidayReplacementsToModel(Model model, List<SelectableHolidayReplacementDto> dtos) {
        model.addAttribute("selectableHolidayReplacements", dtos);
    }

    private static SelectableHolidayReplacementDto toSelectableHolidayReplacementDto(Person person) {
        final SelectableHolidayReplacementDto dto = new SelectableHolidayReplacementDto();
        dto.setPersonId(person.getId());
        dto.setDisplayName(person.getNiceName());
        return dto;
    }

    private static List<Person> holidayReplacementPersonsOfApplication(ApplicationForLeaveForm applicationForLeaveForm) {
        return ofNullable(applicationForLeaveForm.getHolidayReplacements())
            .orElse(emptyList()).stream()
            .map(HolidayReplacementDto::getPerson)
            .toList();
    }

    private void appendDepartmentsToReplacements(ApplicationForLeaveForm appForm) {
        final List<Department> departments = departmentService.getAllDepartments();
        for (HolidayReplacementDto replacementDto : appForm.getHolidayReplacements()) {
            List<String> departmentNames = departmentNamesForPerson(replacementDto.getPerson(), departments);
            replacementDto.setDepartments(departmentNames);
        }
    }

    private ApplicationForLeaveForm mapToApplicationForm(Application application, Locale locale) {

        final List<Department> departments = departmentService.getAllDepartments();

        final List<HolidayReplacementDto> holidayReplacementDtos = application.getHolidayReplacements().stream()
            .map(holidayReplacementEntity -> toDto(holidayReplacementEntity, departments))
            .toList();

        return new ApplicationForLeaveForm.Builder()
            .id(application.getId())
            .address(application.getAddress())
            .startDate(application.getStartDate())
            .startTime(application.getStartTime())
            .endDate(application.getEndDate())
            .endTime(application.getEndTime())
            .teamInformed(application.isTeamInformed())
            .dayLength(application.getDayLength())
            .hoursAndMinutes(application.getHours())
            .person(application.getPerson())
            .vacationType(toApplicationForLeaveFormVacationTypeDto(application.getVacationType(), locale))
            .reason(application.getReason())
            .holidayReplacements(holidayReplacementDtos)
            .build();
    }

    private ApplicationForLeaveFormVacationTypeDto toApplicationForLeaveFormVacationTypeDto(VacationType<?> vacationType, Locale locale) {
        final ApplicationForLeaveFormVacationTypeDto dto = new ApplicationForLeaveFormVacationTypeDto();
        dto.setId(vacationType.getId());
        dto.setLabel(vacationType.getLabel(locale));
        dto.setCategory(vacationType.getCategory());
        return dto;
    }

    private List<String> departmentNamesForPerson(Person person, List<Department> departments) {
        return departments.stream()
            .filter(d -> d.getMembers().contains(person))
            .map(Department::getName)
            .toList();
    }

    private HolidayReplacementDto toDto(HolidayReplacementEntity holidayReplacementEntity, List<Department> departments) {
        final HolidayReplacementDto holidayReplacementDto = new HolidayReplacementDto();
        holidayReplacementDto.setPerson(holidayReplacementEntity.getPerson());
        holidayReplacementDto.setNote(holidayReplacementEntity.getNote());
        holidayReplacementDto.setDepartments(departmentNamesForPerson(holidayReplacementEntity.getPerson(), departments));
        return holidayReplacementDto;
    }

    private boolean isPersonAllowedToExecuteRoleOn(Person applier, Role role, Person person) {

        if (applier.equals(person) || applier.hasRole(OFFICE)) {
            return true;
        }

        if (applier.hasRole(role)) {
            return applier.hasRole(BOSS)
                || departmentService.isDepartmentHeadAllowedToManagePerson(applier, person)
                || departmentService.isSecondStageAuthorityAllowedToManagePerson(applier, person);
        }

        return false;
    }

    private List<Person> getManagedPersons(Person signedInUser) {

        if (signedInUser.hasRole(OFFICE) || (signedInUser.hasRole(BOSS) && signedInUser.hasRole(APPLICATION_ADD))) {
            return personService.getActivePersons();
        }

        final List<Person> membersForDepartmentHead = signedInUser.hasRole(DEPARTMENT_HEAD) && signedInUser.hasRole(APPLICATION_ADD)
            ? departmentService.getManagedMembersOfDepartmentHead(signedInUser)
            : List.of();

        final List<Person> memberForSecondStageAuthority = signedInUser.hasRole(SECOND_STAGE_AUTHORITY) && signedInUser.hasRole(APPLICATION_ADD)
            ? departmentService.getManagedMembersForSecondStageAuthority(signedInUser)
            : List.of();

        return concat(concat(memberForSecondStageAuthority.stream(), membersForDepartmentHead.stream()), Stream.of(signedInUser))
            .filter(person -> !person.hasRole(INACTIVE))
            .distinct()
            .sorted(comparing(Person::getFirstName).thenComparing(Person::getLastName))
            .toList();
    }
}
