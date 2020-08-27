package org.synyx.urlaubsverwaltung.absence.web;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * View controller for legacy reasons to not break urls for the moment (e.g. bookmarks).
 *
 * @since 4.0.0
 * @deprecated in favor of /web/absences
 */
@Deprecated(since = "4.0.0", forRemoval = true)
@Controller
@RequestMapping("/web/application")
public class VacationOverviewViewController {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    @GetMapping("/vacationoverview")
    @ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
    public String redirectToAbsenceOverview() {

        LOG.info("legacy '/web/application/vacationoverview' has been called. redirecting to '/web/absences'");

        return "redirect:/web/absences";
    }
}
