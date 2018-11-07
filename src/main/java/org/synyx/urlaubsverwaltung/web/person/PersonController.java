package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.department.DepartmentConstants;
import org.synyx.urlaubsverwaltung.web.department.UnknownDepartmentException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Controller for management of {@link Person} entities.
 *
 * @author  Aljona Murygina
 */
@Controller
@RequestMapping("/web")
public class PersonController {

    @Autowired
    private PersonService personService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private VacationDaysService vacationDaysService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private WorkingTimeService workingTimeService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/staff/{personId}", method = RequestMethod.GET)
    public String showStaffInformation(@PathVariable("personId") Integer personId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Optional<Integer> requestedYear,
        Model model) throws UnknownPersonException, AccessDeniedException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        Person signedInUser = sessionService.getSignedInUser();

        if (!sessionService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to access data of user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        Integer year = requestedYear.isPresent() ? requestedYear.get() : DateMidnight.now().getYear();

        model.addAttribute(ControllerConstants.YEAR_ATTRIBUTE, year);
        model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, person);

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

        if (account.isPresent()) {
            model.addAttribute("account", account.get());
        }

        return PersonConstants.PERSON_DETAIL_JSP;
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/staff", method = RequestMethod.GET)
    public String showStaff() {

        return "redirect:/web/staff?active=true";
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/staff", method = RequestMethod.GET, params = "active")
    public String showStaff(@RequestParam(value = "active") Boolean active,
        @RequestParam(value = ControllerConstants.DEPARTMENT_ATTRIBUTE, required = false) Optional<Integer> requestedDepartmentId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Optional<Integer> requestedYear,
        Model model) throws UnknownDepartmentException {

        Integer year = requestedYear.isPresent() ? requestedYear.get() : DateMidnight.now().getYear();

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

        return PersonConstants.STAFF_JSP;
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

        return Collections.<Person>emptyList();
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

        return Collections.<Person>emptyList();
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

        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);
        model.addAttribute("accounts", accounts);
        model.addAttribute("vacationDaysLeftMap", vacationDaysLeftMap);
        model.addAttribute(PersonConstants.BEFORE_APRIL_ATTRIBUTE, DateUtil.isBeforeApril(DateMidnight.now(), year));
        model.addAttribute(ControllerConstants.YEAR_ATTRIBUTE, year);
        model.addAttribute("now", DateMidnight.now());

        List<Department> departments = getRelevantDepartments(signedInUser);
        Collections.sort(departments, (a, b) -> a.getName().compareTo(b.getName()));
        model.addAttribute("departments", departments);
    }
}
