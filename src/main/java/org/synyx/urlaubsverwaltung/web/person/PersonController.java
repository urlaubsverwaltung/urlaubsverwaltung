package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

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
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.GravatarUtil;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.department.DepartmentConstants;

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
    private WorkingTimeService workingTimeService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/staff/{personId}", method = RequestMethod.GET)
    public String showStaffInformation(@PathVariable("personId") Integer personId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Integer requestedYear,
        Model model) {

        Optional<Person> optionalPerson = personService.getPersonByID(personId);

        if (optionalPerson.isPresent()) {
            Person person = optionalPerson.get();
            Person signedInUser = sessionService.getSignedInUser();

            boolean isOwnDataPage = person.getId().equals(signedInUser.getId());
            boolean isOffice = signedInUser.hasRole(Role.OFFICE);
            boolean isBoss = signedInUser.hasRole(Role.BOSS);
            boolean isDepartmentHead = departmentService.isDepartmentHeadOfPerson(signedInUser, person);

            if (!isOwnDataPage && !isOffice && !isBoss && !isDepartmentHead) {
                return ControllerConstants.ERROR_JSP;
            }

            Integer year = requestedYear == null ? DateMidnight.now().getYear() : requestedYear;

            model.addAttribute(ControllerConstants.YEAR_ATTRIBUTE, year);
            model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, person);
            model.addAttribute(PersonConstants.GRAVATAR_URL_ATTRIBUTE, GravatarUtil.createImgURL(person.getEmail()));

            model.addAttribute(DepartmentConstants.DEPARTMENTS_ATTRIBUTE,
                departmentService.getAssignedDepartmentsOfMember(person));

            model.addAttribute("workingTimes", workingTimeService.getByPerson(person));

            Optional<Account> account = accountService.getHolidaysAccount(year, person);

            if (account.isPresent()) {
                model.addAttribute("vacationDaysLeft", vacationDaysService.getVacationDaysLeft(account.get()));
                model.addAttribute("account", account.get());
                model.addAttribute(PersonConstants.BEFORE_APRIL_ATTRIBUTE, DateUtil.isBeforeApril(DateMidnight.now()));
            }

            return PersonConstants.PERSON_DETAIL_JSP;
        }

        return ControllerConstants.ERROR_JSP;
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/staff", method = RequestMethod.GET)
    public String showStaff(@RequestParam(value = "active", required = true) Boolean active,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Integer requestedYear,
        Model model) {

        Integer year = requestedYear == null ? DateMidnight.now().getYear() : requestedYear;

        List<Person> persons;

        if (active) {
            persons = getRelevantActivePersons();
        } else {
            persons = getRelevantInactivePersons();
        }

        prepareStaffView(persons, year, model);

        return PersonConstants.STAFF_JSP;
    }


    private List<Person> getRelevantActivePersons() {

        List<Person> persons = personService.getActivePersons();

        // NOTE: If the signed in user is only department head, he wants to see only the persons of his departments
        if (sessionService.getSignedInUser().hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> members = departmentService.getManagedMembersOfDepartmentHead(
                    sessionService.getSignedInUser());

            // NOTE: Only persons without inactive role are relevant
            return members.stream().filter(person -> !person.hasRole(Role.INACTIVE)).collect(Collectors.toList());
        }

        return persons;
    }


    private List<Person> getRelevantInactivePersons() {

        List<Person> persons = personService.getInactivePersons();

        // NOTE: If the signed in user is only department head, he wants to see only the persons of his departments
        if (sessionService.getSignedInUser().hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> members = departmentService.getManagedMembersOfDepartmentHead(
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
        model.addAttribute(ControllerConstants.YEAR_ATTRIBUTE, year);
        model.addAttribute("now", DateMidnight.now());
    }
}
