package org.synyx.urlaubsverwaltung.web.sicknote;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.core.sicknote.statistics.SickNoteStatisticsService;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;


/**
 * Controller for statistics of sick notes resp. sick days.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class SickNoteStatisticsController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SickNoteStatisticsService statisticsService;

    @RequestMapping(value = "/sicknote/statistics", method = RequestMethod.GET, params = "year")
    public String sickNotesStatistics(@RequestParam("year") Integer year, Model model) {

        if (sessionService.isOffice()) {
            SickNoteStatistics statistics = statisticsService.createStatistics(year);

            model.addAttribute("statistics", statistics);

            return "sicknote/sick_notes_statistics";
        }

        return ControllerConstants.ERROR_JSP;
    }
}
