package org.synyx.urlaubsverwaltung.person.web;

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
import org.synyx.urlaubsverwaltung.department.web.UnknownDepartmentException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonConfigurationProperties;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Controller for management of {@link Person} entities.
 */
@Controller
@RequestMapping("/web")
public class PersonViewController {

    private static final String BEFORE_APRIL_ATTRIBUTE = "beforeApril";
    private static final String PERSONS_ATTRIBUTE = "persons";
    private static final String PERSON_ATTRIBUTE = "person";

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final DepartmentService departmentService;
    private final WorkingTimeService workingTimeService;
    private final SettingsService settingsService;
    private final PersonConfigurationProperties personConfigurationProperties;
    private final Clock clock;

    @Autowired
    public PersonViewController(PersonService personService, AccountService accountService,
                                VacationDaysService vacationDaysService, DepartmentService departmentService,
                                WorkingTimeService workingTimeService, SettingsService settingsService, PersonConfigurationProperties personConfigurationProperties, Clock clock) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.departmentService = departmentService;
        this.workingTimeService = workingTimeService;
        this.settingsService = settingsService;
        this.personConfigurationProperties = personConfigurationProperties;
        this.clock = clock;
    }

    @GetMapping("/person/{personId}")
    public String showPersonInformation(@PathVariable("personId") Integer personId,
                                        @RequestParam(value = "year", required = false) Optional<Integer> requestedYear,
                                        Model model) throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        Person signedInUser = personService.getSignedInUser();

        if (!departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(String.format(
                "User '%s' has not the correct permissions to access data of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        Integer year = requestedYear.orElseGet(() ->  Year.now(clock).getValue());

        model.addAttribute("year", year);
        model.addAttribute(PERSON_ATTRIBUTE, person);

        model.addAttribute("departments",
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
    @GetMapping("/person")
    public String showPerson() {

        return "redirect:/web/person?active=true";
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @GetMapping(value = "/person", params = "active")
    public String showPerson(@RequestParam(value = "active") boolean active,
                             @RequestParam(value = "department", required = false) Optional<Integer> requestedDepartmentId,
                             @RequestParam(value = "year", required = false) Optional<Integer> requestedYear,
                             Model model) throws UnknownDepartmentException {

        Integer year = requestedYear.orElseGet(() -> Year.now(clock).getValue());

        Person signedInUser = personService.getSignedInUser();
        final List<Person> persons = active ? getRelevantActivePersons(signedInUser)
            : getRelevantInactivePersons(signedInUser);

        if (requestedDepartmentId.isPresent()) {
            Integer departmentId = requestedDepartmentId.get();
            Department department = departmentService.getDepartmentById(departmentId).orElseThrow(() ->
                new UnknownDepartmentException(departmentId));

            // if department filter is active, only department members are relevant
            persons.retainAll(department.getMembers());

            model.addAttribute("department", department);
        }

        preparePersonView(signedInUser, persons, year, model);

        model.addAttribute("userCanBeManipulated", personConfigurationProperties.isCanBeManipulated());

        return "person/person_view";
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
            List<Person> members = departmentService.getManagedMembersForSecondStageAuthority(signedInUser);

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
            List<Person> members = departmentService.getManagedMembersForSecondStageAuthority(signedInUser);

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


    private void preparePersonView(Person signedInUser, List<Person> persons, int year, Model model) {

        Map<Person, Account> accounts = new HashMap<>();
        Map<Person, VacationDaysLeft> vacationDaysLeftMap = new HashMap<>();

        for (Person person : persons) {
            // get person's account
            Optional<Account> account = accountService.getHolidaysAccount(year, person);

            if (account.isPresent()) {
                Account holidaysAccount = account.get();
                accounts.put(person, holidaysAccount);
                vacationDaysLeftMap.put(person, vacationDaysService.getVacationDaysLeft(holidaysAccount, accountService.getHolidaysAccount(year + 1, person)));
            }
        }

        final LocalDate now = LocalDate.now(clock);

        model.addAttribute(PERSONS_ATTRIBUTE, persons);
        model.addAttribute("accounts", accounts);
        model.addAttribute("vacationDaysLeftMap", vacationDaysLeftMap);
        model.addAttribute(BEFORE_APRIL_ATTRIBUTE, DateUtil.isBeforeApril(now, year));
        model.addAttribute("year", year);
        model.addAttribute("now", now);

        List<Department> departments = getRelevantDepartments(signedInUser);
        departments.sort(Comparator.comparing(Department::getName));
        model.addAttribute("departments", departments);
    }
}
