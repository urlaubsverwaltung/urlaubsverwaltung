package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;

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
    private SessionService sessionService;

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/staff/inactive", method = RequestMethod.GET)
    public String showInactiveStaff() {

        return "redirect:/web/staff/inactive?year=" + DateMidnight.now().getYear();
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/staff", method = RequestMethod.GET)
    public String showActiveStaff() {

        return "redirect:/web/staff?year=" + DateMidnight.now().getYear();
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/staff/inactive", params = ControllerConstants.YEAR_ATTRIBUTE, method = RequestMethod.GET)
    public String showInactiveStaffByYear(@RequestParam(ControllerConstants.YEAR_ATTRIBUTE) int year, Model model) {

        List<Person> persons = getRelevantInactivePersons();
        prepareStaffView(persons, year, model);

        return PersonConstants.STAFF_JSP;
    }


    private List<Person> getRelevantInactivePersons() {

        List<Person> persons = personService.getInactivePersons();

        // NOTE: If the signed in user is only department head, he wants to see only the persons of his departments
        if (sessionService.getSignedInUser().hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> members = departmentService.getAllMembersOfDepartmentsOfPerson(
                    sessionService.getSignedInUser());

            // NOTE: Only persons with inactive role are relevant
            return members.stream().filter(person -> person.hasRole(Role.INACTIVE)).collect(Collectors.toList());
        }

        return persons;
    }


    private void prepareStaffView(List<Person> persons, int year, Model model) {

        Map<Person, String> gravatarUrls = PersonConstants.getGravatarURLs(persons);
        Map<Person, Account> accounts = new HashMap<>();
        Map<Person, VacationDaysLeft> vacationDaysLeftMap = new HashMap<>();

        for (Person person : persons) {
            // get person's account
            Optional<Account> account = accountService.getHolidaysAccount(year, person);

            if (account.isPresent()) {
                Account holidaysAccount = account.get();
                accounts.put(person, holidaysAccount);
                vacationDaysLeftMap.put(person, vacationDaysService.getVacationDaysLeft(holidaysAccount));
            }
        }

        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);
        model.addAttribute(PersonConstants.GRAVATAR_URLS_ATTRIBUTE, gravatarUrls);
        model.addAttribute("accounts", accounts);
        model.addAttribute("vacationDaysLeftMap", vacationDaysLeftMap);
        model.addAttribute(PersonConstants.BEFORE_APRIL_ATTRIBUTE, DateUtil.isBeforeApril(DateMidnight.now()));
        model.addAttribute(ControllerConstants.YEAR_ATTRIBUTE, DateMidnight.now().getYear());
        model.addAttribute("now", DateMidnight.now());
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/staff", params = ControllerConstants.YEAR_ATTRIBUTE, method = RequestMethod.GET)
    public String showActiveStaffByYear(@RequestParam(ControllerConstants.YEAR_ATTRIBUTE) int year, Model model) {

        List<Person> persons = getRelevantActivePersons();
        prepareStaffView(persons, year, model);

        return PersonConstants.STAFF_JSP;
    }


    private List<Person> getRelevantActivePersons() {

        List<Person> persons = personService.getActivePersons();

        // NOTE: If the signed in user is only department head, he wants to see only the persons of his departments
        if (sessionService.getSignedInUser().hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> members = departmentService.getAllMembersOfDepartmentsOfPerson(
                    sessionService.getSignedInUser());

            // NOTE: Only persons without inactive role are relevant
            return members.stream().filter(person -> !person.hasRole(Role.INACTIVE)).collect(Collectors.toList());
        }

        return persons;
    }
}
