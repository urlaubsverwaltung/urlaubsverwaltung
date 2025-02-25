package org.synyx.urlaubsverwaltung.application.export;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;

import static java.lang.Integer.MAX_VALUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.springframework.http.HttpStatus.OK;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

@Controller
@RequestMapping("/web/application")
class ApplicationForLeaveExportViewController implements HasLaunchpad {

    private final PersonService personService;
    private final ApplicationForLeaveExportService applicationForLeaveExportService;
    private final ApplicationForLeaveCsvExportService applicationForLeaveCsvExportService;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;

    @Autowired
    ApplicationForLeaveExportViewController(
        PersonService personService, ApplicationForLeaveExportService applicationForLeaveExportService,
        ApplicationForLeaveCsvExportService applicationForLeaveCsvExportService, DateFormatAware dateFormatAware,
        Clock clock) {

        this.personService = personService;
        this.applicationForLeaveExportService = applicationForLeaveExportService;
        this.applicationForLeaveCsvExportService = applicationForLeaveCsvExportService;
        this.dateFormatAware = dateFormatAware;
        this.clock = clock;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping(value = "/export")
    public ResponseEntity<ByteArrayResource> downloadCsvExport(
        @RequestParam(value = "from", defaultValue = "") String from,
        @RequestParam(value = "to", defaultValue = "") String to,
        @RequestParam(value = "allElements", defaultValue = "false") boolean allElements,
        @RequestParam(value = "query", required = false, defaultValue = "") String query,
        @SortDefault(sort = "person.firstName", direction = Sort.Direction.ASC) Pageable pageable,
        Locale locale
    ) {
        final FilterPeriod period = toFilterPeriod(from, to, locale);

        // NOTE: Not supported at the moment
        if (period.startDate().getYear() != period.endDate().getYear()) {
            return ResponseEntity.badRequest().build();
        }

        final Person signedInUser = personService.getSignedInUser();

        final Pageable adaptedPageable = allElements ? PageRequest.of(0, MAX_VALUE, pageable.getSort()) : pageable;
        final String adaptedQuery = allElements ? "" : query;
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(adaptedPageable, adaptedQuery);

        final Page<ApplicationForLeaveExport> exportPage = applicationForLeaveExportService.getAll(signedInUser, period.startDate(), period.endDate(), pageableSearchQuery);
        final List<ApplicationForLeaveExport> export = exportPage.getContent();
        final CSVFile csvFile = applicationForLeaveCsvExportService.generateCSV(period, locale, export);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", UTF_8));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(csvFile.fileName(), UTF_8).build());

        return ResponseEntity.status(OK).headers(headers).body(csvFile.resource());
    }

    private FilterPeriod toFilterPeriod(String startDateString, String endDateString, Locale locale) {
        final LocalDate firstDayOfYear = Year.now(clock).atDay(1);
        final LocalDate startDate = dateFormatAware.parse(startDateString, locale).orElse(firstDayOfYear);
        final LocalDate endDate = dateFormatAware.parse(endDateString, locale).orElseGet(() -> firstDayOfYear.with(lastDayOfYear()));
        return new FilterPeriod(startDate, endDate);
    }
}
