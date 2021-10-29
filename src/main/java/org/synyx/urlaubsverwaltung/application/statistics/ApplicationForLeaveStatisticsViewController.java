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
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

/**
 * Controller to generate applications for leave statistics.
 */
@Controller
@RequestMapping("/web/application/statistics")
public class ApplicationForLeaveStatisticsViewController {

    private final ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;
    private final ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService;
    private final VacationTypeService vacationTypeService;
    private final DateFormatAware dateFormatAware;

    @Autowired
    public ApplicationForLeaveStatisticsViewController(
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
                                                @RequestParam(value = "to", defaultValue = "") String to,
                                                Model model) {

        final FilterPeriod period = toFilterPeriod(from, to);

        // NOTE: Not supported at the moment
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
    public String downloadCSV(@RequestParam(value = "from", defaultValue = "") String from,
                              @RequestParam(value = "to", defaultValue = "") String to,
                              HttpServletResponse response,
                              Model model)
        throws IOException {

        final FilterPeriod period = toFilterPeriod(from, to);

        // NOTE: Not supported at the moment
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            model.addAttribute("period", period);
            model.addAttribute("errors", "INVALID_PERIOD");

            return "application/app_statistics";
        }

        final String fileName = applicationForLeaveStatisticsCsvExportService.getFileName(period);
        response.setContentType("text/csv");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        final List<ApplicationForLeaveStatistics> statistics = applicationForLeaveStatisticsService.getStatistics(period);
        try (CSVWriter csvWriter = new CSVWriter(response.getWriter())) {
            applicationForLeaveStatisticsCsvExportService.writeStatistics(period, statistics, csvWriter);
        }

        model.addAttribute("period", period);

        return "application/app_statistics";
    }

    private FilterPeriod toFilterPeriod(String startDateString, String endDateString) {
        final LocalDate startDate = dateFormatAware.parse(startDateString).orElse(null);
        final LocalDate endDate = dateFormatAware.parse(endDateString).orElse(null);
        return new FilterPeriod(startDate, endDate);
    }
}
