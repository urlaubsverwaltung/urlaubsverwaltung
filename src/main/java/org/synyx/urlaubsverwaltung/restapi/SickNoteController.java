package org.synyx.urlaubsverwaltung.restapi;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.restapi.wrapper.ResponseWrapper;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Sick Notes", description = "Get all sick notes for a certain period")
@RestController("restApiSickNoteController")
@RequestMapping("/api")
public class SickNoteController {

    private static final String ROOT_URL = "/sicknotes";

    @Autowired
    private SickNoteService sickNoteService;

    @ApiOperation(
        value = "Get all sick notes for a certain period",
        notes = "Get all sick notes for a certain period. Information only reachable for users with role office."
    )
    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    public ResponseWrapper<SickNoteListResponse> sickNotes(
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = "2015-01-01")
        @RequestParam(value = "from", required = true)
        String from,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = "2015-12-31")
        @RequestParam(value = "to", required = true)
        String to) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(RestApiDateFormat.PATTERN);
        DateMidnight startDate = formatter.parseDateTime(from).toDateMidnight();
        DateMidnight endDate = formatter.parseDateTime(to).toDateMidnight();

        List<SickNote> sickNotes = sickNoteService.getByPeriod(startDate, endDate);

        List<AbsenceResponse> sickNoteResponses = sickNotes.stream()
            .filter(SickNote::isActive)
            .map(AbsenceResponse::new)
            .collect(Collectors.toList());

        return new ResponseWrapper<>(new SickNoteListResponse(sickNoteResponses));
    }
}
