package org.synyx.urlaubsverwaltung.application.application;

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
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypePropertyEditor;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;
import org.synyx.urlaubsverwaltung.web.TimePropertyEditor;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationMapper.mapToApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationMapper.merge;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeServiceImpl.convert;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

/**
 * Controller to apply for leave.
 */
@Controller
@RequestMapping("/web")
class ApplicationForLeaveFormViewController {

    private static final Logger LOG = getLogger(lookup().lookupClass());
    private static final String PERSONS_ATTRIBUTE = "persons";
    private static final String PERSON_ATTRIBUTE = "person";
    private static final String SHOW_HALF_DAY_OPTION_ATTRIBUTE = "showHalfDayOption";
    private static final String IS_OFFICE_ATTRIBUTE = "isOffice";
    private static final String REDIRECT_WEB_APPLICATION = "redirect:/web/application/";
    private static final String APP_FORM = "application/app_form";
    private static final String NO_HOLIDAYS_ACCOUNT = "noHolidaysAccount";
    private static final String USER_HAS_NOT_THE_CORRECT_PERMISSIONS = "User '%s' has not the correct permissions to apply for leave for user '%s'";

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final AccountService accountService;
    private final VacationTypeService vacationTypeService;
    private final ApplicationInteractionService applicationInteractionService;
    private final ApplicationForLeaveFormValidator applicationForLeaveFormValidator;
    private final SettingsService settingsService;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;

    @Autowired
    ApplicationForLeaveFormViewController(PersonService personService, DepartmentService departmentService, AccountService accountService,
                                          VacationTypeService vacationTypeService,
                                          ApplicationInteractionService applicationInteractionService,
                                          ApplicationForLeaveFormValidator applicationForLeaveFormValidator,
                                          SettingsService settingsService, DateFormatAware dateFormatAware,
                                          Clock clock) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.accountService = accountService;
        this.vacationTypeService = vacationTypeService;
        this.applicationInteractionService = applicationInteractionService;
        this.applicationForLeaveFormValidator = applicationForLeaveFormValidator;
        this.settingsService = settingsService;
        this.dateFormatAware = dateFormatAware;
        this.clock = clock;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
        binder.registerCustomEditor(Time.class, new TimePropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
        binder.registerCustomEditor(VacationType.class, new VacationTypePropertyEditor(vacationTypeService));
    }

    @GetMapping("/application/new")
    public String newApplicationForm(@RequestParam(value = PERSON_ATTRIBUTE, required = false) Integer personId,
                                     @RequestParam(value = "from", required = false) String startDateString,
                                     @RequestParam(value = "to", required = false) String endDateString,
                                     ApplicationForLeaveForm appForLeaveForm, Model model) {

        final Person signedInUser = personService.getSignedInUser();

        Person person = ofNullable(appForLeaveForm.getPerson())
            .or(getPersonByRequestParam(personId))
            .orElse(signedInUser);

        boolean isApplyingForOneSelf = person.equals(signedInUser);

        if (!isApplyingForOneSelf && !signedInUser.hasRole(OFFICE)) {
            throw new AccessDeniedException(format(USER_HAS_NOT_THE_CORRECT_PERMISSIONS, signedInUser.getId(), person.getId()));
        }

        final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(ZonedDateTime.now(clock).getYear(), person);
        if (holidaysAccount.isPresent()) {

            final LocalDate startDate = dateFormatAware.parse(startDateString).orElse(appForLeaveForm.getStartDate());
            final Supplier<Optional<LocalDate>> endDateSupplier = () -> Optional.ofNullable(appForLeaveForm.getEndDate());

            appForLeaveForm.setStartDate(startDate);
            appForLeaveForm.setEndDate(dateFormatAware.parse(endDateString).or(endDateSupplier).orElse(startDate));

            prepareApplicationForLeaveForm(person, appForLeaveForm, model);
            addSelectableHolidayReplacementsToModel(model, selectableHolidayReplacements(not(isEqual(person))));
        }

        model.addAttribute(NO_HOLIDAYS_ACCOUNT, holidaysAccount.isEmpty());
        return APP_FORM;
    }

    @PostMapping(value = {"/application/new", "/application/{applicationId}"}, params = "add-holiday-replacement")
    public String addHolidayReplacement(ApplicationForLeaveForm applicationForLeaveForm, Model model) {

        final Person signedInUser = personService.getSignedInUser();

        Person person = ofNullable(applicationForLeaveForm.getPerson())
            .orElse(signedInUser);

        boolean isApplyingForOneSelf = person.equals(signedInUser);

        if (!isApplyingForOneSelf && !signedInUser.hasRole(OFFICE)) {
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

            prepareApplicationForLeaveForm(person, applicationForLeaveForm, model);
        }

        model.addAttribute(NO_HOLIDAYS_ACCOUNT, holidaysAccount.isEmpty());

        return APP_FORM;
    }

    @PostMapping(value = {"/application/new/replacements", "/application/{applicationId}/replacements"}, headers = {"X-Requested-With=ajax"})
    public String ajaxAddHolidayReplacement(@ModelAttribute ApplicationForLeaveForm applicationForLeave, Model model) {

        final Person signedInUser = personService.getSignedInUser();
        final Person person = ofNullable(applicationForLeave.getPerson()).orElse(signedInUser);

        final boolean isApplyingForOneSelf = person.equals(signedInUser);

        if (!isApplyingForOneSelf && !signedInUser.hasRole(OFFICE)) {
            throw new AccessDeniedException(format(USER_HAS_NOT_THE_CORRECT_PERMISSIONS, signedInUser.getId(), person.getId()));
        }

        final Person replacementPersonToAdd = applicationForLeave.getHolidayReplacementToAdd();
        if (replacementPersonToAdd != null) {
            final List<Department> departments = departmentService.getAssignedDepartmentsOfMember(replacementPersonToAdd);

            final HolidayReplacementDto replacementDto = new HolidayReplacementDto();
            replacementDto.setPerson(replacementPersonToAdd);
            replacementDto.setDepartments(departments.stream().map(Department::getName).collect(toList()));

            model.addAttribute("holidayReplacement", replacementDto);
        }

        model.addAttribute("index", applicationForLeave.getHolidayReplacements().size());

        final Integer applicationForLeaveId = applicationForLeave.getId();
        if (applicationForLeaveId == null) {
            model.addAttribute("deleteButtonFormActionValue", "/web/application/new");
        } else {
            model.addAttribute("deleteButtonFormActionValue", "/web/application/" + applicationForLeaveId);
        }

        return "thymeleaf/application/application-form :: replacement-item";
    }

    @PostMapping(value = {"/application/new", "/application/{applicationId}"}, params = "remove-holiday-replacement")
    public String removeHolidayReplacement(ApplicationForLeaveForm applicationForLeaveForm,
                                           @RequestParam(name = "remove-holiday-replacement") Integer personIdToRemove,
                                           Model model) {

        final Person signedInUser = personService.getSignedInUser();

        Person person = ofNullable(applicationForLeaveForm.getPerson())
            .orElse(signedInUser);

        boolean isApplyingForOneSelf = person.equals(signedInUser);

        if (!isApplyingForOneSelf && !signedInUser.hasRole(OFFICE)) {
            throw new AccessDeniedException(format(USER_HAS_NOT_THE_CORRECT_PERMISSIONS, signedInUser.getId(), person.getId()));
        }

        final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(ZonedDateTime.now(clock).getYear(), person);
        if (holidaysAccount.isPresent()) {
            final List<HolidayReplacementDto> newList = applicationForLeaveForm.getHolidayReplacements()
                .stream()
                .filter(holidayReplacementDto -> !holidayReplacementDto.getPerson().getId().equals(personIdToRemove))
                .collect(toList());
            applicationForLeaveForm.setHolidayReplacements(newList);
            prepareApplicationForLeaveForm(person, applicationForLeaveForm, model);

            final List<SelectableHolidayReplacementDto> selectableHolidayReplacements = selectableHolidayReplacements(
                personEquals(personIdToRemove)
                    .or(not(containsPerson(holidayReplacementPersonsOfApplication(applicationForLeaveForm))))
                    .and(not(isEqual(person)))
            );
            addSelectableHolidayReplacementsToModel(model, selectableHolidayReplacements);
        }

        model.addAttribute(NO_HOLIDAYS_ACCOUNT, holidaysAccount.isEmpty());

        return APP_FORM;
    }

    @PostMapping("/application")
    public String newApplication(@ModelAttribute("application") ApplicationForLeaveForm appForm, Errors errors,
                                 Model model, RedirectAttributes redirectAttributes) {
        LOG.info("POST new application received: {}", appForm);

        applicationForLeaveFormValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            final Person person = ofNullable(appForm.getPerson()).orElseGet(personService::getSignedInUser);
            final List<SelectableHolidayReplacementDto> selectableHolidayReplacementDtos = selectableHolidayReplacements(
                not(containsPerson(holidayReplacementPersonsOfApplication(appForm)))
                    .and(not(isEqual(person)))
            );
            addSelectableHolidayReplacementsToModel(model, selectableHolidayReplacementDtos);

            prepareApplicationForLeaveForm(appForm.getPerson(), appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            LOG.debug("new application ({}) has errors: {}", appForm, errors);
            return APP_FORM;
        }

        final Application app = mapToApplication(appForm);
        final Person applier = personService.getSignedInUser();

        final Application savedApplicationForLeave;
        if (app.getVacationType().isRequiresApproval()) {
            savedApplicationForLeave = applicationInteractionService.apply(app, applier, ofNullable(appForm.getComment()));
        } else {
            savedApplicationForLeave = applicationInteractionService.directAllow(app, applier, ofNullable(appForm.getComment()));
        }
        LOG.debug("new application has been saved {}", savedApplicationForLeave);

        redirectAttributes.addFlashAttribute("applySuccess", true);

        return REDIRECT_WEB_APPLICATION + savedApplicationForLeave.getId();
    }

    @GetMapping("/application/{applicationId}/edit")
    public String editApplicationForm(@PathVariable("applicationId") Integer applicationId, Model model) {

        return applicationInteractionService.get(applicationId)
            .filter(application -> WAITING.equals(application.getStatus()))
            .map(this::mapToApplicationForm)
            .map(applicationForLeaveForm -> this.editApplicationForm(applicationForLeaveForm, model))
            .orElse("application/app_notwaiting");
    }

    private String editApplicationForm(ApplicationForLeaveForm applicationForLeaveForm, Model model) {
        final Person signedInUser = personService.getSignedInUser();

        final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(Year.now(clock).getValue(), signedInUser);
        if (holidaysAccount.isPresent()) {
            prepareApplicationForLeaveForm(signedInUser, applicationForLeaveForm, model);

            final List<SelectableHolidayReplacementDto> selectableHolidayReplacements = selectableHolidayReplacements(
                not(containsPerson(holidayReplacementPersonsOfApplication(applicationForLeaveForm)))
                    .and(not(isEqual(signedInUser)))
            );
            model.addAttribute("selectableHolidayReplacements", selectableHolidayReplacements);
        }

        model.addAttribute(NO_HOLIDAYS_ACCOUNT, holidaysAccount.isEmpty());
        model.addAttribute("application", applicationForLeaveForm);

        return APP_FORM;
    }

    @PostMapping("/application/{applicationId}")
    public String sendEditApplicationForm(@PathVariable("applicationId") Integer applicationId,
                                          @ModelAttribute("application") ApplicationForLeaveForm appForm, Errors errors,
                                          Model model, RedirectAttributes redirectAttributes) throws UnknownApplicationForLeaveException {

        final Optional<Application> maybeApplication = applicationInteractionService.get(applicationId);
        if (maybeApplication.isEmpty()) {
            throw new UnknownApplicationForLeaveException(applicationId);
        }

        final Application application = maybeApplication.get();
        if (application.getStatus().compareTo(WAITING) != 0) {
            redirectAttributes.addFlashAttribute("editError", true);
            return REDIRECT_WEB_APPLICATION + applicationId;
        }

        final Person signedInUser = personService.getSignedInUser();

        appForm.setId(application.getId());
        applicationForLeaveFormValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareApplicationForLeaveForm(appForm.getPerson(), appForm, model);
            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            addSelectableHolidayReplacementsToModel(model, selectableHolidayReplacements(
                not(containsPerson(holidayReplacementPersonsOfApplication(appForm)))
                    .and(not(isEqual(signedInUser))))
            );

            LOG.debug("edit application ({}) has errors: {}", appForm, errors);
            return APP_FORM;
        }

        final Application editedApplication = merge(application, appForm);
        final Application savedApplicationForLeave;
        try {
            savedApplicationForLeave = applicationInteractionService.edit(application, editedApplication, signedInUser, Optional.ofNullable(appForm.getComment()));
        } catch (EditApplicationForLeaveNotAllowedException e) {
            return "application/app_notwaiting";
        }

        LOG.debug("Edited application with success applied {}", savedApplicationForLeave);

        redirectAttributes.addFlashAttribute("editSuccess", true);

        return REDIRECT_WEB_APPLICATION + savedApplicationForLeave.getId();
    }

    private Supplier<Optional<? extends Person>> getPersonByRequestParam(Integer personId) {
        if (personId == null) {
            return Optional::empty;
        }
        return () -> personService.getPersonByID(personId);
    }

    private void prepareApplicationForLeaveForm(Person person, ApplicationForLeaveForm appForm, Model model) {

        final List<Person> persons = personService.getActivePersons();
        model.addAttribute(PERSON_ATTRIBUTE, person);
        model.addAttribute(PERSONS_ATTRIBUTE, persons);
        model.addAttribute("canAddApplicationForLeaveForAnotherUser", personService.getSignedInUser().hasRole(OFFICE));

        final boolean overtimeActive = settingsService.getSettings().getOvertimeSettings().isOvertimeActive();
        model.addAttribute("overtimeActive", overtimeActive);

        List<VacationType> activeVacationTypes = vacationTypeService.getActiveVacationTypes();
        if (!overtimeActive) {
            activeVacationTypes = vacationTypeService.getActiveVacationTypesWithoutCategory(OVERTIME);
        }
        model.addAttribute("vacationTypes", activeVacationTypes);

        appendDepartmentsToReplacements(appForm);
        model.addAttribute("application", appForm);

        final boolean isHalfDayApplication = ofNullable(appForm.getDayLength()).filter(DayLength::isHalfDay).isPresent();
        final boolean isHalfDaysActivated = settingsService.getSettings().getApplicationSettings().isAllowHalfDays();
        model.addAttribute(SHOW_HALF_DAY_OPTION_ATTRIBUTE, isHalfDayApplication || isHalfDaysActivated);
    }

    private static Predicate<Person> personEquals(Person person) {
        return person::equals;
    }

    private static Predicate<Person> personEquals(Integer personId) {
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
            .collect(toUnmodifiableList());
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
            .collect(toList());
    }

    private void appendDepartmentsToReplacements(ApplicationForLeaveForm appForm) {
        final List<Department> departments = departmentService.getAllDepartments();

        for (HolidayReplacementDto replacementDto : appForm.getHolidayReplacements()) {
            List<String> departmentNames = departmentNamesForPerson(replacementDto.getPerson(), departments);
            replacementDto.setDepartments(departmentNames);
        }
    }

    private ApplicationForLeaveForm mapToApplicationForm(Application application) {

        final List<Department> departments = departmentService.getAllDepartments();

        final List<HolidayReplacementDto> holidayReplacementDtos = application.getHolidayReplacements().stream()
            .map(holidayReplacementEntity -> toDto(holidayReplacementEntity, departments))
            .collect(toList());

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
            .vacationType(convert(application.getVacationType()))
            .reason(application.getReason())
            .holidayReplacements(holidayReplacementDtos)
            .build();
    }

    private List<String> departmentNamesForPerson(Person person, List<Department> departments) {
        return departments.stream()
            .filter(d -> d.getMembers().contains(person))
            .map(Department::getName)
            .collect(toList());
    }

    private HolidayReplacementDto toDto(HolidayReplacementEntity holidayReplacementEntity, List<Department> departments) {
        final HolidayReplacementDto holidayReplacementDto = new HolidayReplacementDto();
        holidayReplacementDto.setPerson(holidayReplacementEntity.getPerson());
        holidayReplacementDto.setNote(holidayReplacementEntity.getNote());
        holidayReplacementDto.setDepartments(departmentNamesForPerson(holidayReplacementEntity.getPerson(), departments));
        return holidayReplacementDto;
    }
}
