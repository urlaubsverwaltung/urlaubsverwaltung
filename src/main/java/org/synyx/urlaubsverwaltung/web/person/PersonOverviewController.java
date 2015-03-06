package org.synyx.urlaubsverwaltung.web.person;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.statistics.UsedDaysOverview;
import org.synyx.urlaubsverwaltung.web.util.GravatarUtil;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Controller for the different ways of displaying the personal overview page.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class PersonOverviewController {

    private static final Logger LOG = Logger.getLogger(PersonOverviewController.class);

    @Autowired
    private PersonService personService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OwnCalendarService calendarService;

    @Autowired
    private SickNoteService sickNoteService;

    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    public String showOverview(@RequestParam(value = ControllerConstants.YEAR, required = false) String year) {

        if (sessionService.isInactive()) {
            return ControllerConstants.ERROR_JSP;
        }

        Person user = sessionService.getLoggedUser();

        if (StringUtils.hasText(year)) {
            return "redirect:/web/staff/" + user.getId() + "/overview?year=" + year;
        }

        return "redirect:/web/staff/" + user.getId() + "/overview";
    }


    @RequestMapping(value = "/staff/{personId}/overview", method = RequestMethod.GET)
    public String showOverview(@PathVariable("personId") Integer personId,
        @RequestParam(value = ControllerConstants.YEAR, required = false) String year, Model model) {

        if (sessionService.isInactive()) {
            return ControllerConstants.ERROR_JSP;
        }

        Person person = personService.getPersonByID(personId);
        Person loggedUser = sessionService.getLoggedUser();

        boolean isOwnOverviewPage = person.getId().equals(loggedUser.getId());

        if (!isOwnOverviewPage && !sessionService.isOffice() && !sessionService.isBoss()) {
            return ControllerConstants.ERROR_JSP;
        }

        model.addAttribute(PersonConstants.LOGGED_USER, loggedUser);
        model.addAttribute(ControllerConstants.PERSON, person);

        String url = GravatarUtil.createImgURL(person.getEmail());
        model.addAttribute("gravatar", url);

        Integer yearToShow = parseYearParameter(year);
        prepareApplications(person, yearToShow, model);
        prepareHolidayAccounts(person, yearToShow, model);
        prepareSickNoteList(person, yearToShow, model);

        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());

        return "person/overview";
    }


    /**
     * Parses the year of the given String, if parsing fails, the current year is returned.
     *
     * @param  input
     *
     * @return  parsed Integer of the given String, if parsing failed the current year is returned
     */
    private Integer parseYearParameter(String input) {

        // default value for year is the current year
        Integer year = DateMidnight.now(GregorianChronology.getInstance()).getYear();

        if (StringUtils.hasText(input)) {
            try {
                year = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                LOG.warn("Tried to show overview for an invalid year entry: " + input, ex);
            }
        }

        return year;
    }


    private void prepareSickNoteList(Person person, int year, Model model) {

        List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, DateUtil.getFirstDayOfYear(year),
                DateUtil.getLastDayOfYear(year));

        BigDecimal sickDays = BigDecimal.ZERO;
        BigDecimal sickDaysWithAUB = BigDecimal.ZERO;
        BigDecimal childSickDays = BigDecimal.ZERO;
        BigDecimal childSickDaysWithAUB = BigDecimal.ZERO;

        for (SickNote sickNote : sickNotes) {
            if (!sickNote.isActive()) {
                continue;
            }

            if (sickNote.getType().equals(SickNoteType.SICK_NOTE_CHILD)) {
                childSickDays = childSickDays.add(sickNote.getWorkDays());

                if (sickNote.isAubPresent()) {
                    BigDecimal workDays = calendarService.getWorkDays(DayLength.FULL, sickNote.getAubStartDate(),
                            sickNote.getAubEndDate(), person);
                    childSickDaysWithAUB = childSickDaysWithAUB.add(workDays);
                }
            } else {
                sickDays = sickDays.add(sickNote.getWorkDays());

                if (sickNote.isAubPresent()) {
                    BigDecimal workDays = calendarService.getWorkDays(DayLength.FULL, sickNote.getAubStartDate(),
                            sickNote.getAubEndDate(), person);
                    sickDaysWithAUB = sickDaysWithAUB.add(workDays);
                }
            }
        }

        Collections.sort(sickNotes, new Comparator<SickNote>() {

                @Override
                public int compare(SickNote o1, SickNote o2) {

                    // show latest sick notes at first
                    return o2.getStartDate().compareTo(o1.getStartDate());
                }
            });

        model.addAttribute("sickDays", sickDays);
        model.addAttribute("sickDaysWithAUB", sickDaysWithAUB);
        model.addAttribute("childSickDays", childSickDays);
        model.addAttribute("childSickDaysWithAUB", childSickDaysWithAUB);
        model.addAttribute("sickNotes", sickNotes);
    }


    private void prepareApplications(Person person, int year, Model model) {

        // get the person's applications for the given year
        List<Application> apps = applicationService.getApplicationsForACertainPeriodAndPerson(DateUtil
                .getFirstDayOfYear(year), DateUtil.getLastDayOfYear(year), person);

        if (!apps.isEmpty()) {
            List<Application> applications = new ArrayList<>();

            for (Application a : apps) {
                if ((a.getStatus() != ApplicationStatus.CANCELLED) || (a.isFormerlyAllowed())) {
                    applications.add(a);
                }
            }

            Collections.sort(applications, new Comparator<Application>() {

                    @Override
                    public int compare(Application o1, Application o2) {

                        // show latest applications at first
                        return o2.getStartDate().compareTo(o1.getStartDate());
                    }
                });

            model.addAttribute(ControllerConstants.APPLICATIONS, applications);

            UsedDaysOverview usedDaysOverview = new UsedDaysOverview(applications, year, calendarService);
            model.addAttribute("usedDaysOverview", usedDaysOverview);
        }
    }


    private void prepareHolidayAccounts(Person person, int year, Model model) {

        // get person's holidays account and entitlement for the given year
        Account account = accountService.getHolidaysAccount(year, person);

        if (account != null) {
            model.addAttribute("vacationDaysLeft", calculationService.getVacationDaysLeft(account));
            model.addAttribute(PersonConstants.BEFORE_APRIL, DateUtil.isBeforeApril(DateMidnight.now()));
        }

        model.addAttribute(ControllerConstants.ACCOUNT, account);
    }
}
