package org.synyx.urlaubsverwaltung.staff.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.department.web.DepartmentConstants;
import org.synyx.urlaubsverwaltung.department.web.UnknownDepartmentException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;


/**
 * Controller for management of {@link Person} entities.
 */
@Controller
@RequestMapping("/web")
public class StaffController {

    private static final String BEFORE_APRIL_ATTRIBUTE = "beforeApril";
    private static final String PERSONS_ATTRIBUTE = "persons";
    private static final String PERSON_ATTRIBUTE = "person";

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final DepartmentService departmentService;
    private final WorkingTimeService workingTimeService;
    private final SettingsService settingsService;
    private final SessionService sessionService;

    @Autowired
    public StaffController(PersonService personService, AccountService accountService, VacationDaysService vacationDaysService, DepartmentService departmentService, WorkingTimeService workingTimeService, SettingsService settingsService, SessionService sessionService) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.departmentService = departmentService;
        this.workingTimeService = workingTimeService;
        this.settingsService = settingsService;
        this.sessionService = sessionService;
    }

    @GetMapping("/staff/{personId}")
    public String showStaffInformation(@PathVariable("personId") Integer personId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Optional<Integer> requestedYear,
        Model model) throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        Person signedInUser = sessionService.getSignedInUser();

        if (!sessionService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to access data of user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        Integer year = requestedYear.orElseGet(() -> ZonedDateTime.now(UTC).getYear());

        model.addAttribute(ControllerConstants.YEAR_ATTRIBUTE, year);
        model.addAttribute(PERSON_ATTRIBUTE, person);

        model.addAttribute(DepartmentConstants.DEPARTMENTS_ATTRIBUTE,
            departmentService.getAssignedDepartmentsOfMember(person));

        Optional<WorkingTime> workingTime = workingTimeService.getCurrentOne(person);
        Optional<FederalState> optionalFederalState = Optional.empty();

        if (workingTime.isPresent()) {
            model.addAttribute("workingTime", workingTime.get());
            optionalFederalState = workingTime.get().getFederalStateOverride();
        }

        if (optionalFederalState.isPresent()) {
            model.addAttribute("federalState", optionalFederalState.get());
        } else {
            model.addAttribute("federalState",
                settingsService.getSettings().getWorkingTimeSettings().getFederalState());
        }

        Optional<Account> account = accountService.getHolidaysAccount(year, person);
        account.ifPresent(account1 -> model.addAttribute("account", account1));

        return "person/person_detail";
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @GetMapping("/staff")
    public String showStaff() {

        return "redirect:/web/staff?active=true";
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @GetMapping(value = "/staff", params = "active")
    public String showStaff(@RequestParam(value = "active") Boolean active,
        @RequestParam(value = ControllerConstants.DEPARTMENT_ATTRIBUTE, required = false) Optional<Integer> requestedDepartmentId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Optional<Integer> requestedYear,
        Model model) throws UnknownDepartmentException {

        Integer year = requestedYear.orElseGet(() -> ZonedDateTime.now(UTC).getYear());

        Person signedInUser = sessionService.getSignedInUser();
        final List<Person> persons = active ? getRelevantActivePersons(signedInUser)
                                            : getRelevantInactivePersons(signedInUser);

        if (requestedDepartmentId.isPresent()) {
            Integer departmentId = requestedDepartmentId.get();
            Department department = departmentService.getDepartmentById(departmentId).orElseThrow(() ->
                        new UnknownDepartmentException(departmentId));

            // if department filter is active, only department members are relevant
            persons.retainAll(department.getMembers());

            model.addAttribute(ControllerConstants.DEPARTMENT_ATTRIBUTE, department);
        }

        prepareStaffView(signedInUser, persons, year, model);

        return "person/staff_view";
    }


    private List<Person> getRelevantActivePersons(Person signedInUser) {

        if (signedInUser.hasRole(Role.BOSS) || signedInUser.hasRole(Role.OFFICE)) {
            return personService.getActivePersons();
        }

        // NOTE: If the signed in user is only department head, he wants to see only the persons of his departments
        if (signedInUser.hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> members = departmentService.getManagedMembersOfDepartmentHead(signedInUser);

            // NOTE: Only persons without inactive role are relevant
            return members.stream().filter(person -> !person.hasRole(Role.INACTIVE)).collect(Collectors.toList());
        }

        // NOTE: If the signed in user is second stage authority, he wants to see only the persons of his departments
        if (signedInUser.hasRole(Role.SECOND_STAGE_AUTHORITY)) {
            List<Person> members = departmentService.getMembersForSecondStageAuthority(signedInUser);

            // NOTE: Only persons without inactive role are relevant
            return members.stream().filter(person -> !person.hasRole(Role.INACTIVE)).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }


    private List<Person> getRelevantInactivePersons(Person signedInUser) {

        if (signedInUser.hasRole(Role.BOSS) || signedInUser.hasRole(Role.OFFICE)) {
            return personService.getInactivePersons();
        }

        // NOTE: If the signed in user is only department head, he wants to see only the persons of his departments
        if (signedInUser.hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> members = departmentService.getManagedMembersOfDepartmentHead(signedInUser);

            // NOTE: Only persons with inactive role are relevant
            return members.stream().filter(person -> person.hasRole(Role.INACTIVE)).collect(Collectors.toList());
        }

        // NOTE: If the signed in user is second stage authority, he wants to see only the persons of his departments
        if (signedInUser.hasRole(Role.SECOND_STAGE_AUTHORITY)) {
            List<Person> members = departmentService.getMembersForSecondStageAuthority(signedInUser);

            // NOTE: Only persons with inactive role are relevant
            return members.stream().filter(person -> person.hasRole(Role.INACTIVE)).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }


    private List<Department> getRelevantDepartments(Person signedInUser) {

        if (signedInUser.hasRole(Role.BOSS) || signedInUser.hasRole(Role.OFFICE)) {
            return departmentService.getAllDepartments();
        }

        // NOTE: If the signed in user is only department head, he wants to see only the persons of his departments
        if (signedInUser.hasRole(Role.DEPARTMENT_HEAD)) {
            return departmentService.getManagedDepartmentsOfDepartmentHead(signedInUser);
        }

        // NOTE: If the signed in user is second stage authority, he wants to see only the persons of his departments
        if (signedInUser.hasRole(Role.SECOND_STAGE_AUTHORITY)) {
            return departmentService.getManagedDepartmentsOfSecondStageAuthority(signedInUser);
        }

        // normal users can see their own departments only
        return departmentService.getAssignedDepartmentsOfMember(signedInUser);
    }


    private void prepareStaffView(Person signedInUser, List<Person> persons, int year, Model model) {

        Map<Person, Account> accounts = new HashMap<>();
        Map<Person, VacationDaysLeft> vacationDaysLeftMap = new HashMap<>();

        for (Person person : persons) {
            // get person's account
            Optional<Account> account = accountService.getHolidaysAccount(year, person);

            if (account.isPresent()) {
                Account holidaysAccount = account.get();
                accounts.put(person, holidaysAccount);
                vacationDaysLeftMap.put(person, vacationDaysService.getVacationDaysLeft(holidaysAccount, accountService.getHolidaysAccount(year+1, person)));
            }
        }

        model.addAttribute(PERSONS_ATTRIBUTE, persons);
        model.addAttribute("accounts", accounts);
        model.addAttribute("vacationDaysLeftMap", vacationDaysLeftMap);
        model.addAttribute(BEFORE_APRIL_ATTRIBUTE, DateUtil.isBeforeApril(LocalDate.now(UTC), year));
        model.addAttribute(ControllerConstants.YEAR_ATTRIBUTE, year);
        model.addAttribute("now", LocalDate.now(UTC));

        List<Department> departments = getRelevantDepartments(signedInUser);
        departments.sort(Comparator.comparing(Department::getName));
        model.addAttribute("departments", departments);
    }
}
