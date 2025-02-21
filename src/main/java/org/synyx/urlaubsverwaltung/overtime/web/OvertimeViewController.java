package org.synyx.urlaubsverwaltung.overtime.web;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.validation.Valid;
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
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.overtime.web.OvertimeListMapper.mapToDto;
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
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    OvertimeViewController(OvertimeService overtimeService, PersonService personService,
                           OvertimeFormValidator validator, DepartmentService departmentService,
                           ApplicationService applicationService, VacationTypeViewModelService vacationTypeViewModelService,
                           SettingsService settingsService, Clock clock) {
        this.overtimeService = overtimeService;
        this.personService = personService;
        this.validator = validator;
        this.departmentService = departmentService;
        this.applicationService = applicationService;
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
    public String showPersonalOvertime() {
        final Person signedInUser = personService.getSignedInUser();
        return "redirect:/web/overtime?person=" + signedInUser.getId();
    }

    @GetMapping(value = "/overtime", params = "person")
    public String showOvertime(
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

        final boolean userIsAllowedToWriteOvertime = overtimeService.isUserIsAllowedToWriteOvertime(signedInUser, person);

        final OvertimeListDto overtimeListDto = mapToDto(
            getOvertimeAbsences(selectedYear, person),
            overtimeService.getOvertimeRecordsForPersonAndYear(person, selectedYear),
            overtimeService.getTotalOvertimeForPersonAndYear(person, selectedYear),
            overtimeService.getTotalOvertimeForPersonBeforeYear(person, selectedYear),
            overtimeService.getLeftOvertimeForPerson(person),
            signedInUser,
            userIsAllowedToWriteOvertime, selectedYear);

        model.addAttribute("records", overtimeListDto.getRecords());
        model.addAttribute("overtimeTotal", overtimeListDto.getOvertimeTotal());
        model.addAttribute("overtimeTotalLastYear", overtimeListDto.getOvertimeTotalLastYear());
        model.addAttribute("overtimeLeft", overtimeListDto.getOvertimeLeft());
        model.addAttribute("userIsAllowedToWriteOvertime", userIsAllowedToWriteOvertime);
        model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(person));

        return "overtime/overtime_list";
    }

    @GetMapping("/overtime/{id}")
    public String showOvertimeDetails(@PathVariable("id") Long id, Model model) throws UnknownOvertimeException {

        final Overtime overtime = overtimeService.getOvertimeById(id).orElseThrow(() -> new UnknownOvertimeException(id));
        final Person person = overtime.getPerson();
        final Person signedInUser = personService.getSignedInUser();

        if (!departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to see overtime records of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        final OvertimeDetailsDto overtimeDetailsDto = OvertimeDetailsMapper.mapToDto(
            overtime,
            overtimeService.getCommentsForOvertime(overtime),
            overtimeService.getTotalOvertimeForPersonAndYear(person, overtime.getEndDate().getYear()),
            overtimeService.getLeftOvertimeForPerson(person));

        final int currentYear = Year.now(clock).getValue();
        model.addAttribute("currentYear", currentYear);


        model.addAttribute("record", overtimeDetailsDto.getRecord());
        model.addAttribute("comments", overtimeDetailsDto.getComments());
        model.addAttribute("overtimeTotal", overtimeDetailsDto.getOvertimeTotal());
        model.addAttribute("overtimeLeft", overtimeDetailsDto.getOvertimeLeft());
        model.addAttribute("userIsAllowedToWriteOvertime", overtimeService.isUserIsAllowedToWriteOvertime(signedInUser, person));
        model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(person));

        return "overtime/overtime_details";
    }

    @GetMapping("/overtime/new")
    public String recordOvertime(
        @RequestParam(value = "person", required = false) Long personId, Model model)
        throws UnknownPersonException {

        final Person signedInUser = personService.getSignedInUser();
        final Person person;

        if (personId != null) {
            person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        } else {
            person = signedInUser;
        }

        if (!overtimeService.isUserIsAllowedToWriteOvertime(signedInUser, person)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to record overtime for user '%s'",
                signedInUser.getId(), person.getId()));
        }

        final OvertimeForm overtimeForm = new OvertimeForm();
        prepareModelForCreation(model, signedInUser, person, overtimeForm);

        return "overtime/overtime_form";
    }

    @PostMapping("/overtime")
    public String recordOvertime(@Valid @ModelAttribute("overtime") OvertimeForm overtimeForm, Errors errors,
                                 Model model, RedirectAttributes redirectAttributes) {

        final Person signedInUser = personService.getSignedInUser();
        final Person person = overtimeForm.getPerson();

        if (!overtimeService.isUserIsAllowedToWriteOvertime(signedInUser, person)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to record overtime for user '%s'",
                signedInUser.getId(), person.getId()));
        }

        validator.validate(overtimeForm, errors);
        if (errors.hasErrors()) {
            prepareModelForCreation(model, signedInUser, person, overtimeForm);
            return "overtime/overtime_form";
        }

        final Overtime overtime = overtimeForm.generateOvertime();
        final Optional<String> overtimeFormComment = Optional.ofNullable(overtimeForm.getComment());
        final Overtime recordedOvertime = overtimeService.save(overtime, overtimeFormComment, signedInUser);

        redirectAttributes.addFlashAttribute("overtimeRecord", OvertimeCommentAction.CREATED.name());
        return "redirect:/web/overtime/" + recordedOvertime.getId();
    }

    @GetMapping("/overtime/{id}/edit")
    public String editOvertime(@PathVariable("id") Long id, Model model) throws UnknownOvertimeException {

        final Overtime overtime = overtimeService.getOvertimeById(id).orElseThrow(() -> new UnknownOvertimeException(id));
        final Person signedInUser = personService.getSignedInUser();
        final Person person = overtime.getPerson();

        if (!overtimeService.isUserIsAllowedToWriteOvertime(signedInUser, person)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to edit overtime record of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        prepareModelForEdit(model, signedInUser, person, new OvertimeForm(overtime));

        return "overtime/overtime_form";
    }

    @PostMapping("/overtime/{id}")
    public String updateOvertime(@PathVariable("id") Long id,
                                 @ModelAttribute("overtime") OvertimeForm overtimeForm, Errors errors,
                                 Model model, RedirectAttributes redirectAttributes) throws UnknownOvertimeException {

        final Overtime overtime = overtimeService.getOvertimeById(id).orElseThrow(() -> new UnknownOvertimeException(id));
        final Person signedInUser = personService.getSignedInUser();
        final Person person = overtime.getPerson();

        if (!overtimeService.isUserIsAllowedToWriteOvertime(signedInUser, person)) {
            throw new AccessDeniedException(format(
                "User '%s' has not the correct permissions to edit overtime record of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        validator.validate(overtimeForm, errors);
        if (errors.hasErrors()) {
            prepareModelForEdit(model, signedInUser, person, overtimeForm);
            return "overtime/overtime_form";
        }

        overtimeForm.updateOvertime(overtime);
        overtimeService.save(overtime, Optional.ofNullable(overtimeForm.getComment()), signedInUser);

        redirectAttributes.addFlashAttribute("overtimeRecord", OvertimeCommentAction.EDITED.name());
        return "redirect:/web/overtime/" + id;
    }

    private void prepareModelForCreation(Model model, Person signedInUser, Person person, OvertimeForm overtimeForm) {

        if (signedInUser.hasRole(DEPARTMENT_HEAD) || signedInUser.hasRole(SECOND_STAGE_AUTHORITY)) {
            final List<Person> persons = departmentService.getManagedActiveMembersOfPerson(signedInUser);
            model.addAttribute("persons", persons);
        }

        if (signedInUser.hasRole(OFFICE) || signedInUser.hasRole(BOSS)) {
            final List<Person> persons = personService.getActivePersons();
            model.addAttribute("persons", persons);
        }

        prepareModelForEdit(model, signedInUser, person, overtimeForm);
    }

    private void prepareModelForEdit(Model model, Person signedInUser, Person person, OvertimeForm overtimeForm) {
        model.addAttribute("overtime", overtimeForm);
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
