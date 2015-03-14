package org.synyx.urlaubsverwaltung.web.person;

import com.google.common.base.Optional;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.util.GravatarUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Controller for management of {@link Person} entities.
 *
 * @author  Aljona Murygina
 */
@Controller
public class PersonController {

    // links
    private static final String ACTIVE_LINK = "/staff";
    private static final String INACTIVE_LINK = "/staff/inactive";

    @Autowired
    private PersonService personService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private SessionService sessionService;

    /**
     * Shows list with inactive staff, default: for current year.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = INACTIVE_LINK, method = RequestMethod.GET)
    public String showInactiveStaff(Model model) {

        if (sessionService.isOffice() || sessionService.isBoss()) {
            model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());

            List<Person> persons = personService.getInactivePersons();

            if (persons.isEmpty()) {
                model.addAttribute("notexistent", true);
                model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());
            } else {
                prepareStaffView(persons, DateMidnight.now().getYear(), model);
            }

            return PersonConstants.STAFF_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Shows list with active staff, default: for current year.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ACTIVE_LINK, method = RequestMethod.GET)
    public String showActiveStaff(Model model) {

        if (sessionService.isOffice() || sessionService.isBoss()) {
            model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());

            List<Person> persons = personService.getActivePersons();
            prepareStaffView(persons, DateMidnight.now().getYear(), model);

            return PersonConstants.STAFF_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Shows list with inactive staff for the given year.
     *
     * @param  year
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = INACTIVE_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showInactiveStaffByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (sessionService.isOffice() || sessionService.isBoss()) {
            model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());

            List<Person> persons = personService.getInactivePersons();

            if (persons.isEmpty()) {
                model.addAttribute("notexistent", true);
                model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());
            } else {
                prepareStaffView(persons, year, model);
            }

            return PersonConstants.STAFF_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Shows list with active staff for the given year.
     *
     * @param  year
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ACTIVE_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showActiveStaffByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (sessionService.isOffice() || sessionService.isBoss()) {
            model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());

            List<Person> persons = personService.getActivePersons();
            prepareStaffView(persons, year, model);

            return PersonConstants.STAFF_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    private void prepareStaffView(List<Person> persons, int year, Model model) {

        Map<Person, String> gravatarUrls = new HashMap<>();
        Map<Person, Account> accounts = new HashMap<>();
        Map<Person, VacationDaysLeft> vacationDaysLeftMap = new HashMap<>();

        for (Person person : persons) {
            // get url of person's gravatar image
            String url = GravatarUtil.createImgURL(person.getEmail());

            if (url != null) {
                gravatarUrls.put(person, url);
            }

            // get person's account
            Optional<Account> account = accountService.getHolidaysAccount(year, person);

            if (account.isPresent()) {
                Account holidaysAccount = account.get();
                accounts.put(person, holidaysAccount);
                vacationDaysLeftMap.put(person, calculationService.getVacationDaysLeft(holidaysAccount));
            }
        }

        model.addAttribute("persons", persons);
        model.addAttribute(PersonConstants.GRAVATAR_URLS, gravatarUrls);
        model.addAttribute("accounts", accounts);
        model.addAttribute("vacationDaysLeftMap", vacationDaysLeftMap);
        model.addAttribute(PersonConstants.BEFORE_APRIL, DateUtil.isBeforeApril(DateMidnight.now()));
        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());
        model.addAttribute("now", DateMidnight.now());
    }
}
