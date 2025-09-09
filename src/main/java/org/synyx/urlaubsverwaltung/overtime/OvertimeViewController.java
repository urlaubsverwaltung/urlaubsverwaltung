package org.synyx.urlaubsverwaltung.overtime;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.validation.Valid;
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
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.COMMENTED;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeListMapper.mapToDto;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * Manage overtime of persons.
 */
@Controller
@RequestMapping("/web")
public class OvertimeViewController implements HasLaunchpad {

    private final OvertimeService overtimeService;
    private final PersonService personService;
    private final OvertimeFormValidator validator;
    private final DepartmentService departmentService;
    private final ApplicationService applicationService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final SettingsService settingsService;
    private final Clock clock;

    OvertimeViewController(
        OvertimeService overtimeService,
        PersonService personService,
        OvertimeFormValidator validator,
        DepartmentService departmentService,
        ApplicationService applicationService,
        WorkingTimeCalendarService workingTimeCalendarService,
        VacationTypeViewModelService vacationTypeViewModelService,
        SettingsService settingsService,
        Clock clock
    ) {
        this.overtimeService = overtimeService;
        this.personService = personService;
        this.validator = validator;
        this.departmentService = departmentService;
        this.applicationService = applicationService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.vacationTypeViewModelService = vacationTypeViewModelService;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }

    @GetMapping("/overtime")
    public String showOvertimeOverview() {
        final Person signedInUser = personService.getSignedInUser();
        return "redirect:/web/overtime?person=" + signedInUser.getId();
    }

    @GetMapping(value = "/overtime", params = "person")
    public String showOvertimeOverviewOfPerson(
        @RequestParam(value = "person") Long personId,
        @RequestParam(value = "year", required = false) Integer requestedYear, Model model)
        throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        final Person signedInUser = personService.getSignedInUser();

        if (!departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to see overtime records of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        final int currentYear = Year.now(clock).getValue();
        final int selectedYear = requestedYear != null ? requestedYear : currentYear;
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("selectedYear", selectedYear);

        model.addAttribute("person", person);

        final Predicate<Overtime> userIsAllowedToUpdateOvertime =
            overtime -> overtimeService.isUserIsAllowedToUpdateOvertime(signedInUser, person, overtime);

        final List<Application> overtimeAbsences = getOvertimeAbsences(selectedYear, person);
        final Map<PersonId, WorkingTimeCalendar> workingTimeCalendarByPersonId = getWorkingTimeCalendars(overtimeAbsences);

        final OvertimeListDto overtimeListDto = mapToDto(
            overtimeAbsences,
            overtimeService.getOvertimeRecordsForPersonAndYear(person, selectedYear),
            overtimeService.getTotalOvertimeForPersonAndYear(person, selectedYear),
            overtimeService.getTotalOvertimeForPersonBeforeYear(person, selectedYear),
            overtimeService.getLeftOvertimeForPerson(person),
            signedInUser,
            userIsAllowedToUpdateOvertime,
            (id, range) -> workingTimeCalendarByPersonId.get(id),
            selectedYear
        );

        model.addAttribute("records", overtimeListDto.getRecords());
        model.addAttribute("overtimeTotal", overtimeListDto.getOvertimeTotal());
        model.addAttribute("overtimeTotalLastYear", overtimeListDto.getOvertimeTotalLastYear());
        model.addAttribute("overtimeLeft", overtimeListDto.getOvertimeLeft());

        final boolean userIsAllowedToCreateOvertime = overtimeService.isUserIsAllowedToCreateOvertime(signedInUser, person);
        model.addAttribute("userIsAllowedToCreateOvertime", userIsAllowedToCreateOvertime);
        model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(person));

        return "overtime/overtime_list";
    }

    private Map<PersonId, WorkingTimeCalendar> getWorkingTimeCalendars(Collection<Application> applications) {

        if (applications.isEmpty()) {
            return Map.of();
        }

        final Set<Person> persons = new HashSet<>();

        LocalDate from = LocalDate.MAX;
        LocalDate to = LocalDate.MIN;

        for (Application application : applications) {
            if (application.getVacationType().isOfCategory(OVERTIME)) {
                from = application.getStartDate().isBefore(from) ? application.getStartDate() : from;
                to = application.getEndDate().isAfter(to) ? application.getEndDate() : to;
                persons.add(application.getPerson());
            }
        }

        return workingTimeCalendarService.getWorkingTimesByPersons(persons, new DateRange(from, to))
            .entrySet().stream()
            .collect(toMap(entry -> entry.getKey().getIdAsPersonId(), Map.Entry::getValue));
    }

    @GetMapping("/overtime/{id}")
    public String showOvertime(@PathVariable("id") Long id, Model model) throws UnknownOvertimeException {

        final Overtime overtime = overtimeService.getOvertimeById(new OvertimeId(id)).orElseThrow(() -> new UnknownOvertimeException(id));
        final Person person = personService.getPersonByID(overtime.personId().value()).orElseThrow(() -> new IllegalStateException("expected person to exist."));
        final Person signedInUser = personService.getSignedInUser();

        if (!departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to see overtime records of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        final List<OvertimeComment> overtimeComments = overtimeService.getCommentsForOvertime(overtime.id());
        final Set<PersonId> personIds = overtimeComments.stream().map(OvertimeComment::personId).flatMap(Optional::stream).collect(toSet());
        personIds.add(overtime.personId());
        personIds.add(signedInUser.getIdAsPersonId());

        final Map<PersonId, Person> personById = personService.getAllPersonsByIds(personIds).stream()
            .collect(toMap(Person::getIdAsPersonId, identity()));

        final OvertimeDetailsDto overtimeDetailsDto = OvertimeDetailsMapper.mapToDto(
            overtime,
            overtimeService.getCommentsForOvertime(overtime.id()),
            overtimeService.getTotalOvertimeForPersonAndYear(person, overtime.endDate().getYear()),
            overtimeService.getLeftOvertimeForPerson(person),
            personById::get
        );

        final int currentYear = Year.now(clock).getValue();
        model.addAttribute("currentYear", currentYear);

        model.addAttribute("record", overtimeDetailsDto.getRecord());
        model.addAttribute("comments", overtimeDetailsDto.getComments());
        model.addAttribute("overtimeTotal", overtimeDetailsDto.getOvertimeTotal());
        model.addAttribute("overtimeLeft", overtimeDetailsDto.getOvertimeLeft());
        model.addAttribute("comment", new OvertimeCommentFormDto());
        model.addAttribute("userIsAllowedToUpdateOvertime", overtimeService.isUserIsAllowedToUpdateOvertime(signedInUser, person, overtime));
        model.addAttribute("userIsAllowedToAddOvertimeComment", overtimeService.isUserIsAllowedToAddOvertimeComment(signedInUser, person));
        model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(person));

        return "overtime/overtime_details";
    }

    @GetMapping("/overtime/new")
    public String showNewOvertime(@RequestParam(value = "person", required = false) Long personId, Model model) throws UnknownPersonException {

        final Person signedInUser = personService.getSignedInUser();
        final Person person;

        if (personId != null) {
            person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        } else {
            person = signedInUser;
        }

        if (!overtimeService.isUserIsAllowedToCreateOvertime(signedInUser, person)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to record overtime for user '%s'",
                signedInUser.getId(), person.getId()));
        }

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto();
        prepareModelForCreation(model, signedInUser, person, overtimeFormDto);

        return "overtime/overtime_form";
    }

    @PostMapping("/overtime")
    public String createNewOvertime(
        @Valid @ModelAttribute("overtime") OvertimeFormDto overtimeFormDto, Errors errors,
        Model model, RedirectAttributes redirectAttributes
    ) {

        final Person signedInUser = personService.getSignedInUser();
        final Person person = overtimeFormDto.getPerson();

        if (!overtimeService.isUserIsAllowedToCreateOvertime(signedInUser, person)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to record overtime for user '%s'",
                signedInUser.getId(), person.getId()));
        }

        validator.validate(overtimeFormDto, errors);
        if (errors.hasErrors()) {
            prepareModelForCreation(model, signedInUser, person, overtimeFormDto);
            return "overtime/overtime_form";
        }

        final DateRange dateRange = new DateRange(overtimeFormDto.getStartDate(), overtimeFormDto.getEndDate());
        final Duration duration = overtimeFormDto.getDuration();
        final PersonId authorId = signedInUser.getIdAsPersonId();
        final Overtime overtime = overtimeService.createOvertime(person.getIdAsPersonId(), dateRange, duration, authorId, overtimeFormDto.getComment());

        redirectAttributes.addFlashAttribute("overtimeRecord", OvertimeCommentAction.CREATED.name());
        return "redirect:/web/overtime/" + overtime.id().value();
    }

    @GetMapping("/overtime/{id}/edit")
    public String showOvertimeEdit(@PathVariable("id") Long id, Model model) throws UnknownOvertimeException {

        final Overtime overtime = overtimeService.getOvertimeById(new OvertimeId(id)).orElseThrow(() -> new UnknownOvertimeException(id));
        final Person signedInUser = personService.getSignedInUser();
        final Person person = personService.getPersonByID(overtime.personId().value()).orElseThrow(() -> new IllegalStateException("expected person to exist."));

        if (!overtimeService.isUserIsAllowedToUpdateOvertime(signedInUser, person, overtime)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to edit overtime record of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        final BigDecimal overtimeHours = BigDecimal.valueOf((double) overtime.duration().toMinutes() / 60);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto();
        overtimeFormDto.setId(overtime.id().value());
        overtimeFormDto.setPerson(person);
        overtimeFormDto.setStartDate(overtime.startDate());
        overtimeFormDto.setEndDate(overtime.endDate());
        overtimeFormDto.setHours(overtimeHours.setScale(0, RoundingMode.DOWN).abs());
        overtimeFormDto.setMinutes(overtimeHours.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_EVEN).abs().intValueExact());
        overtimeFormDto.setReduce(overtimeHours.doubleValue() < 0);

        prepareModelForEdit(model, signedInUser, person, overtimeFormDto);

        return "overtime/overtime_form";
    }

    @PostMapping("/overtime/{id}")
    public String updateOvertime(
        @PathVariable("id") Long id,
        @Valid @ModelAttribute("overtime") OvertimeFormDto overtimeFormDto, Errors errors,
        Model model, RedirectAttributes redirectAttributes
    ) throws UnknownOvertimeException {

        final Overtime overtime = overtimeService.getOvertimeById(new OvertimeId(id)).orElseThrow(() -> new UnknownOvertimeException(id));
        final Person signedInUser = personService.getSignedInUser();
        final Person person = personService.getPersonByID(overtime.personId().value()).orElseThrow(() -> new IllegalStateException("expected person to exist."));

        if (!overtimeService.isUserIsAllowedToUpdateOvertime(signedInUser, person, overtime)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to edit overtime record of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        validator.validate(overtimeFormDto, errors);
        if (errors.hasErrors()) {
            prepareModelForEdit(model, signedInUser, person, overtimeFormDto);
            return "overtime/overtime_form";
        }

        final DateRange dateRange = new DateRange(overtimeFormDto.getStartDate(), overtimeFormDto.getEndDate());
        final Duration duration = overtimeFormDto.getDuration();
        final PersonId authorId = signedInUser.getIdAsPersonId();
        overtimeService.updateOvertime(new OvertimeId(id), dateRange, duration, authorId, overtimeFormDto.getComment());

        redirectAttributes.addFlashAttribute("overtimeRecord", OvertimeCommentAction.EDITED.name());
        return "redirect:/web/overtime/" + id;
    }

    @PostMapping("/overtime/{id}/comment")
    public String addComment(
        @PathVariable("id") Long id,
        @ModelAttribute("comment") OvertimeCommentFormDto comment
    ) throws UnknownOvertimeException {

        final OvertimeId overtimeId = new OvertimeId(id);
        final Overtime overtime = overtimeService.getOvertimeById(overtimeId).orElseThrow(() -> new UnknownOvertimeException(id));
        final Person signedInUser = personService.getSignedInUser();
        final Person person = personService.getPersonByID(overtime.personId().value()).orElseThrow(() -> new IllegalStateException("expected person to exist."));

        if (!overtimeService.isUserIsAllowedToAddOvertimeComment(signedInUser, person)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to add overtime comment of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        overtimeService.saveComment(overtimeId, COMMENTED, comment.getText(), signedInUser);

        return "redirect:/web/overtime/" + overtime.id().value();
    }

    private void prepareModelForCreation(Model model, Person signedInUser, Person person, OvertimeFormDto overtimeFormDto) {

        if (signedInUser.hasRole(DEPARTMENT_HEAD) || signedInUser.hasRole(SECOND_STAGE_AUTHORITY)) {
            final List<Person> persons = departmentService.getManagedActiveMembersOfPerson(signedInUser);
            model.addAttribute("persons", persons);
        }

        if (signedInUser.hasRole(OFFICE) || signedInUser.hasRole(BOSS)) {
            final List<Person> persons = personService.getActivePersons();
            model.addAttribute("persons", persons);
        }

        prepareModelForEdit(model, signedInUser, person, overtimeFormDto);
    }

    private void prepareModelForEdit(Model model, Person signedInUser, Person person, OvertimeFormDto overtimeFormDto) {
        model.addAttribute("overtime", overtimeFormDto);
        model.addAttribute("person", person);

        final OvertimeSettings overtimeSettings = settingsService.getSettings().getOvertimeSettings();

        boolean canAddOvertimeForAnotherUser = signedInUser.hasRole(OFFICE) || (signedInUser.isPrivileged() && overtimeSettings.isOvertimeWritePrivilegedOnly());
        model.addAttribute("canAddOvertimeForAnotherUser", canAddOvertimeForAnotherUser);

        model.addAttribute("overtimeReductionPossible", overtimeSettings.isOvertimeReductionWithoutApplicationActive());

        final List<VacationTypeDto> vacationTypeColors = vacationTypeViewModelService.getVacationTypeColors();
        model.addAttribute("vacationTypeColors", vacationTypeColors);
    }

    private List<Application> getOvertimeAbsences(int year, Person person) {
        final LocalDate firstDayOfYear = Year.of(year).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        return applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(firstDayOfYear, lastDayOfYear, person, activeStatuses(), OVERTIME);
    }
}
