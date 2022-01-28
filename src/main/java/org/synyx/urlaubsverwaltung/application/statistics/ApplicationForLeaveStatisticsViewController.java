package org.synyx.urlaubsverwaltung.application.statistics;

import liquibase.util.csv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static liquibase.util.csv.CSVReader.DEFAULT_QUOTE_CHARACTER;
import static liquibase.util.csv.opencsv.CSVWriter.NO_QUOTE_CHARACTER;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

/**
 * Controller to generate applications for leave statistics.
 */
@Controller
@RequestMapping("/web/application/statistics")
class ApplicationForLeaveStatisticsViewController {

    protected static final byte[] UTF8_BOM = new byte[]{(byte) 239, (byte) 187, (byte) 191};
    public static final char SEPARATOR = ';';

    private final ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;
    private final ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService;
    private final VacationTypeService vacationTypeService;
    private final DateFormatAware dateFormatAware;

    @Autowired
    ApplicationForLeaveStatisticsViewController(
        ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService,
        ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService,
        VacationTypeService vacationTypeService, DateFormatAware dateFormatAware) {

        this.applicationForLeaveStatisticsService = applicationForLeaveStatisticsService;
        this.applicationForLeaveStatisticsCsvExportService = applicationForLeaveStatisticsCsvExportService;
        this.vacationTypeService = vacationTypeService;
        this.dateFormatAware = dateFormatAware;
    }

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @PostMapping
    public String applicationForLeaveStatistics(@ModelAttribute("period") FilterPeriod period) {

        final String startDateIsoString = dateFormatAware.formatISO(period.getStartDate());
        final String endDateIsoString = dateFormatAware.formatISO(period.getEndDate());
        return "redirect:/web/application/statistics?from=" + startDateIsoString + "&to=" + endDateIsoString;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping
    public String applicationForLeaveStatistics(@RequestParam(value = "from", defaultValue = "") String from,
                                                @RequestParam(value = "to", defaultValue = "") String to, Model model) {

        final FilterPeriod period = toFilterPeriod(from, to);
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            model.addAttribute("period", period);
            model.addAttribute("errors", "INVALID_PERIOD");
            return "application/app_statistics";
        }

        model.addAttribute("period", period);
        model.addAttribute("from", period.getStartDate());
        model.addAttribute("to", period.getEndDate());
        model.addAttribute("statistics", applicationForLeaveStatisticsService.getStatistics(period));
        model.addAttribute("vacationTypes", vacationTypeService.getAllVacationTypes());

        return "application/app_statistics";
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping(value = "/download")
    public void downloadCSV(@RequestParam(value = "from", defaultValue = "") String from,
                            @RequestParam(value = "to", defaultValue = "") String to,
                            HttpServletResponse response) throws IOException {

        final FilterPeriod period = toFilterPeriod(from, to);

        // NOTE: Not supported at the moment
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            response.sendError(SC_BAD_REQUEST);
            return;
        }

        final String fileName = applicationForLeaveStatisticsCsvExportService.getFileName(period);
        response.setContentType("text/csv");
        response.setCharacterEncoding(UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        final List<ApplicationForLeaveStatistics> statistics = applicationForLeaveStatisticsService.getStatistics(period);

        try (final OutputStream os = response.getOutputStream()) {
            os.write(UTF8_BOM);

            try (final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(os, UTF_8))) {
                try (final CSVWriter csvWriter = new CSVWriter(printWriter, SEPARATOR, NO_QUOTE_CHARACTER, DEFAULT_QUOTE_CHARACTER)) {
                    applicationForLeaveStatisticsCsvExportService.writeStatistics(period, statistics, csvWriter);
                }
                printWriter.flush();
            }
        }
    }

    private FilterPeriod toFilterPeriod(String startDateString, String endDateString) {
        final LocalDate startDate = dateFormatAware.parse(startDateString).orElse(null);
        final LocalDate endDate = dateFormatAware.parse(endDateString).orElse(null);
        return new FilterPeriod(startDate, endDate);
    }
}
