package org.synyx.urlaubsverwaltung.application.export;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
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
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;

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

    /**
     * Exports the absences of the requested persons.
     *
     * <p>
     * This export is not a statistic, therefore it knows nothing about the sorting of the statistics page it is
     * linked from. The persons to export are named explicitly with {@code personIds} instead.
     *
     * @param allElements export every person instead of the given {@code personIds}
     * @param personIds   persons to export, ids the signed-in user must not access are ignored
     */
    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping(value = "/export")
    public ResponseEntity<ByteArrayResource> downloadCsvExport(
        @RequestParam(value = "from", defaultValue = "") String from,
        @RequestParam(value = "to", defaultValue = "") String to,
        @RequestParam(value = "allElements", defaultValue = "false") boolean allElements,
        @RequestParam(value = "personIds", required = false, defaultValue = "") List<Long> personIds,
        Locale locale
    ) {
        final FilterPeriod period = toFilterPeriod(from, to, locale);

        // NOTE: Not supported at the moment
        if (period.startDate().getYear() != period.endDate().getYear()) {
            return ResponseEntity.badRequest().build();
        }

        final Person signedInUser = personService.getSignedInUser();

        final List<ApplicationForLeaveExport> export = allElements
            ? applicationForLeaveExportService.getAll(signedInUser, period.startDate(), period.endDate())
            : applicationForLeaveExportService.getAllForPersons(signedInUser, period.startDate(), period.endDate(), personIds.stream().map(PersonId::new).toList());

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
