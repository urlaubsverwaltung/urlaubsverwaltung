package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.csv.CSVFile;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.OK;

/**
 * Controller for statistics of sick notes resp. sick days.
 */
@Controller
@RequestMapping("/web/sickdays/statistics")
class SickDaysStatisticsViewController {

    private final SickDaysStatisticsService sickDaysStatisticsService;
    private final SickDaysDetailedStatisticsCsvExportService sickDaysDetailedStatisticsCsvExportService;
    private final PersonService personService;
    private final DateFormatAware dateFormatAware;

    @Autowired
    SickDaysStatisticsViewController(SickDaysStatisticsService sickDaysStatisticsService,
                                     SickDaysDetailedStatisticsCsvExportService sickDaysDetailedStatisticsCsvExportService,
                                     PersonService personService, DateFormatAware dateFormatAware) {
        this.sickDaysStatisticsService = sickDaysStatisticsService;
        this.sickDaysDetailedStatisticsCsvExportService = sickDaysDetailedStatisticsCsvExportService;
        this.personService = personService;
        this.dateFormatAware = dateFormatAware;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_VIEW')")
    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadCSV(@RequestParam(value = "from", defaultValue = "") String from,
                                                         @RequestParam(value = "to", defaultValue = "") String to,
                                                         @RequestParam(value = "query", required = false, defaultValue = "") String query,
                                                         @SortDefault.SortDefaults({@SortDefault(sort = "person.firstName", direction = Sort.Direction.ASC)})
                                                         Pageable pageable) {

        final FilterPeriod period = toFilterPeriod(from, to);

        // NOTE: Not supported at the moment
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            return ResponseEntity.badRequest().build();
        }

        final Person signedInUser = personService.getSignedInUser();

        final Page<SickDaysDetailedStatistics> sickDaysStatisticsPage =
                sickDaysStatisticsService.getAll(signedInUser, period.getStartDate(), period.getEndDate(), new PageableSearchQuery(pageable, query));

        final CSVFile csvFile = sickDaysDetailedStatisticsCsvExportService.generateCSV(period, sickDaysStatisticsPage.getContent());

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv"));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(csvFile.getFileName()).build());

        return ResponseEntity.status(OK).headers(headers).body(csvFile.getResource());
    }

    private FilterPeriod toFilterPeriod(String startDateString, String endDateString) {
        final LocalDate startDate = dateFormatAware.parse(startDateString).orElse(null);
        final LocalDate endDate = dateFormatAware.parse(endDateString).orElse(null);
        return new FilterPeriod(startDate, endDate);
    }
}
