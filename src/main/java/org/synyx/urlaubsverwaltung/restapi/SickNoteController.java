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

import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class SickNoteController {

    private static final String ROOT_URL = "/sicknotes";

    @Autowired
    private SickNoteService sickNoteService;

    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    @ModelAttribute("response")
    public SickNoteListResponse vacations(@RequestParam(value = "start", required = true) String start,
        @RequestParam(value = "end", required = true) String end) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(RestApiDateFormat.PATTERN);
        DateMidnight startDate = formatter.parseDateTime(start).toDateMidnight();
        DateMidnight endDate = formatter.parseDateTime(end).toDateMidnight();

        List<SickNote> sickNotes = sickNoteService.getByPeriod(startDate, endDate);

        List<AbsenceResponse> sickNoteResponses = Lists.transform(sickNotes,
                new Function<SickNote, AbsenceResponse>() {

                    @Override
                    public AbsenceResponse apply(SickNote sickNote) {

                        return new AbsenceResponse(sickNote);
                    }
                });

        return new SickNoteListResponse(sickNoteResponses);
    }
}
