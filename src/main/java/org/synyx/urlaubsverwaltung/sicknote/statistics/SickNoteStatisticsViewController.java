package org.synyx.urlaubsverwaltung.sicknote.statistics;

import liquibase.util.csv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static liquibase.util.csv.CSVReader.DEFAULT_QUOTE_CHARACTER;
import static liquibase.util.csv.opencsv.CSVWriter.NO_QUOTE_CHARACTER;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

/**
 * Controller for statistics of sick notes resp. sick days.
 */
@Controller
@RequestMapping("/web/sicknote/statistics")
class SickNoteStatisticsViewController {

    protected static final byte[] UTF8_BOM = new byte[]{(byte) 239, (byte) 187, (byte) 191};
    public static final char SEPARATOR = ';';
    private final SickNoteDetailedStatisticsService statisticsService;
    private final SickNoteDetailedStatisticsCsvExportService sickNoteDetailedStatisticsCsvExportService;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;

    @Autowired
    SickNoteStatisticsViewController(SickNoteDetailedStatisticsService statisticsService,
                                     SickNoteDetailedStatisticsCsvExportService sickNoteDetailedStatisticsCsvExportService, DateFormatAware dateFormatAware, Clock clock) {
        this.statisticsService = statisticsService;
        this.sickNoteDetailedStatisticsCsvExportService = sickNoteDetailedStatisticsCsvExportService;
        this.dateFormatAware = dateFormatAware;
        this.clock = clock;
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping
    public String sickNotesStatistics(@RequestParam(value = "year", required = false) Integer requestedYear, Model model) {

        final Clock clockOfRequestedYear = getClockOfRequestedYear(requestedYear);
        final SickNoteStatistics statistics = statisticsService.createStatistics(clockOfRequestedYear);

        model.addAttribute("statistics", statistics);

        return "sicknote/sick_notes_statistics";
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping(value = "/download")
    public void downloadCSV(@RequestParam(value = "from", defaultValue = "") String from,
                            @RequestParam(value = "to", defaultValue = "") String to,
                            HttpServletResponse response) throws IOException {

        final FilterPeriod period = toFilterPeriod(from, to);

        final String fileName = sickNoteDetailedStatisticsCsvExportService.getFileName(period);
        response.setContentType("text/csv");
        response.setCharacterEncoding(UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        List<SickNoteDetailedStatistics> allDetailedSicknotes = statisticsService.getAllSicknotes(period);

        try (final OutputStream os = response.getOutputStream()) {
            os.write(UTF8_BOM);

            try (final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(os, UTF_8))) {
                try (final CSVWriter csvWriter = new CSVWriter(printWriter, SEPARATOR, NO_QUOTE_CHARACTER, DEFAULT_QUOTE_CHARACTER)) {
                    sickNoteDetailedStatisticsCsvExportService.writeStatistics(period, allDetailedSicknotes, csvWriter);
                }
                printWriter.flush();
            }
        }
    }

    private Clock getClockOfRequestedYear(Integer requestedYear) {
        if (requestedYear == null) {
            requestedYear = Year.now(clock).getValue();
        }
        return Clock.fixed(ZonedDateTime.now(clock).withYear(requestedYear).toInstant(), clock.getZone());
    }

    private FilterPeriod toFilterPeriod(String startDateString, String endDateString) {
        final LocalDate startDate = dateFormatAware.parse(startDateString).orElse(null);
        final LocalDate endDate = dateFormatAware.parse(endDateString).orElse(null);
        return new FilterPeriod(startDate, endDate);
    }
}
