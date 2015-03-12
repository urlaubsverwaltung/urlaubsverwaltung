package org.synyx.urlaubsverwaltung.web.application;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.FilterRequest;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.util.GravatarUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Controller for showing applications for leave in a certain state.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RequestMapping("/application")
@Controller
public class ApplicationForLeaveController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OwnCalendarService calendarService;

    /**
     * Show waiting applications for leave on default.
     *
     * @return  waiting applications for leave page or error page if not boss or office
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showDefault() {

        if (sessionService.isBoss() || sessionService.isOffice()) {
            return "redirect:/web/application/waiting";
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Show all applications for leave, not dependent on their status.
     *
     * @param  year  if not given, the current year is used to display applications for leave for
     * @param  model
     *
     * @return  all applications for leave for the given year or for the current year if no year is given
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String showAll(@RequestParam(value = ControllerConstants.YEAR, required = false) Integer year, Model model) {

        if (sessionService.isBoss() || sessionService.isOffice()) {
            int yearToDisplay = year == null ? DateMidnight.now().getYear() : year;

            List<ApplicationForLeave> applicationsForLeave = getAllRelevantApplicationsForLeave(yearToDisplay);

            model.addAttribute("applications", applicationsForLeave);
            model.addAttribute(PersonConstants.GRAVATAR_URLS, getAllRelevantGravatarUrls(applicationsForLeave));
            model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());
            model.addAttribute("titleApp", "applications.all");
            model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());
            model.addAttribute("filterRequest", new FilterRequest());

            return ControllerConstants.APPLICATIONS_URL + "/app_list";
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Get all relevant applications for leave, i.e. not cancelled applications for leave and cancelled but formerly
     * allowed applications for leave.
     *
     * @param  year  to get applications for leave for
     *
     * @return  all relevant applications for leave
     */
    private List<ApplicationForLeave> getAllRelevantApplicationsForLeave(int year) {

        DateMidnight firstDay = DateUtil.getFirstDayOfYear(year);
        DateMidnight lastDay = DateUtil.getLastDayOfYear(year);

        List<Application> applications = applicationService.getApplicationsForACertainPeriod(firstDay, lastDay);

        return FluentIterable.from(applications).filter(new Predicate<Application>() {

                    @Override
                    public boolean apply(Application application) {

                        boolean isNotCancelled = !application.hasStatus(ApplicationStatus.CANCELLED);
                        boolean isCancelledButWasAllowed = application.hasStatus(ApplicationStatus.CANCELLED)
                            && application.isFormerlyAllowed();

                        return isNotCancelled || isCancelledButWasAllowed;
                    }
                }).transform(new Function<Application, ApplicationForLeave>() {

                    @Override
                    public ApplicationForLeave apply(Application input) {

                        return new ApplicationForLeave(input, calendarService);
                    }
                }).toList();
    }


    /**
     * Get all gravatar urls for the persons of the given applications for leave.
     *
     * @param  applications  of the persons for that gravatar urls should be fetched for
     *
     * @return  gravatar urls mapped to applications for leave
     */
    private Map<Application, String> getAllRelevantGravatarUrls(List<ApplicationForLeave> applications) {

        Map<Application, String> gravatarUrls = new HashMap<>();

        for (Application application : applications) {
            String gravatarUrl = GravatarUtil.createImgURL(application.getPerson().getEmail());

            if (gravatarUrl != null) {
                gravatarUrls.put(application, gravatarUrl);
            }
        }

        return gravatarUrls;
    }


    /**
     * Show waiting applications for leave.
     *
     * @param  year  if not given, the current year is used to display applications for leave for
     * @param  model
     *
     * @return  waiting applications for leave for the given year or for the current year if no year is given
     */
    @RequestMapping(value = "/waiting", method = RequestMethod.GET)
    public String showWaiting(@RequestParam(value = ControllerConstants.YEAR, required = false) Integer year,
        Model model) {

        if (sessionService.isBoss() || sessionService.isOffice()) {
            int yearToDisplay = year == null ? DateMidnight.now().getYear() : year;

            return prepareRelevantApplicationsForLeave(ApplicationStatus.WAITING, yearToDisplay, model);
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    private String prepareRelevantApplicationsForLeave(ApplicationStatus status, int year, Model model) {

        String title = "";

        if (status == ApplicationStatus.WAITING) {
            title = "applications.waiting";
        } else if (status == ApplicationStatus.ALLOWED) {
            title = "applications.allowed";
        } else if (status == ApplicationStatus.CANCELLED) {
            title = "applications.cancelled";
        } else if (status == ApplicationStatus.REJECTED) {
            title = "applications.rejected";
        }

        DateMidnight firstDay = DateUtil.getFirstDayOfYear(year);
        DateMidnight lastDay = DateUtil.getLastDayOfYear(year);

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndState(firstDay, lastDay,
                status);

        List<ApplicationForLeave> applicationsToBeShown = FluentIterable.from(applications).filter(
                new Predicate<Application>() {

                    @Override
                    public boolean apply(Application input) {

                        boolean isNotCancelled = !input.hasStatus(ApplicationStatus.CANCELLED);
                        boolean isCancelledButWasAllowed = input.hasStatus(ApplicationStatus.CANCELLED)
                            && input.isFormerlyAllowed();

                        return isNotCancelled || isCancelledButWasAllowed;
                    }
                }).transform(new Function<Application, ApplicationForLeave>() {

                    @Override
                    public ApplicationForLeave apply(Application input) {

                        return new ApplicationForLeave(input, calendarService);
                    }
                }).toList();

        Map<Application, String> gravatarUrls = getAllRelevantGravatarUrls(applicationsToBeShown);

        model.addAttribute(PersonConstants.GRAVATAR_URLS, gravatarUrls);
        model.addAttribute("applications", applicationsToBeShown);
        model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());
        model.addAttribute("titleApp", title);
        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());
        model.addAttribute("filterRequest", new FilterRequest());

        return ControllerConstants.APPLICATIONS_URL + "/app_list";
    }


    /**
     * Show allowed applications for leave.
     *
     * @param  year  if not given, the current year is used to display applications for leave for
     * @param  model
     *
     * @return  allowed applications for leave for the given year or for the current year if no year is given
     */
    @RequestMapping(value = "/allowed", method = RequestMethod.GET)
    public String showAllowed(@RequestParam(value = ControllerConstants.YEAR, required = false) Integer year,
        Model model) {

        if (sessionService.isBoss() || sessionService.isOffice()) {
            int yearToDisplay = year == null ? DateMidnight.now().getYear() : year;

            return prepareRelevantApplicationsForLeave(ApplicationStatus.ALLOWED, yearToDisplay, model);
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Show cancelled applications for leave.
     *
     * @param  year  if not given, the current year is used to display applications for leave for
     * @param  model
     *
     * @return  cancelled applications for leave for the given year or for the current year if no year is given
     */
    @RequestMapping(value = "/cancelled", method = RequestMethod.GET)
    public String showCancelled(@RequestParam(value = ControllerConstants.YEAR, required = false) Integer year,
        Model model) {

        if (sessionService.isBoss() || sessionService.isOffice()) {
            int yearToDisplay = year == null ? DateMidnight.now().getYear() : year;

            return prepareRelevantApplicationsForLeave(ApplicationStatus.CANCELLED, yearToDisplay, model);
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Show rejected applications for leave.
     *
     * @param  year  if not given, the current year is used to display applications for leave for
     * @param  model
     *
     * @return  rejected applications for leave for the given year or for the current year if no year is given
     */
    @RequestMapping(value = "/rejected", method = RequestMethod.GET)
    public String showRejected(@RequestParam(value = ControllerConstants.YEAR, required = false) Integer year,
        Model model) {

        if (sessionService.isBoss() || sessionService.isOffice()) {
            int yearToDisplay = year == null ? DateMidnight.now().getYear() : year;

            return prepareRelevantApplicationsForLeave(ApplicationStatus.REJECTED, yearToDisplay, model);
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }
}
