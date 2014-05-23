package org.synyx.urlaubsverwaltung.restapi;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller("restApiVacationController")
public class VacationController {

    private static final String ROOT_URL = "/vacation";

    @Autowired
    private ApplicationService applicationService;

    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    @ModelAttribute("response")
    public VacationListResponse vacations(@RequestParam(value = "from", required = true) String from,
        @RequestParam(value = "to", required = true) String to) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(RestApiDateFormat.PATTERN);
        DateMidnight startDate = formatter.parseDateTime(from).toDateMidnight();
        DateMidnight endDate = formatter.parseDateTime(to).toDateMidnight();

        List<Application> applications = applicationService.getAllowedApplicationsForACertainPeriod(startDate, endDate);

        List<AbsenceResponse> vacationResponses = Lists.transform(applications,
                new Function<Application, AbsenceResponse>() {

                    @Override
                    public AbsenceResponse apply(Application application) {

                        return new AbsenceResponse(application);
                    }
                });

        return new VacationListResponse(vacationResponses);
    }
}
