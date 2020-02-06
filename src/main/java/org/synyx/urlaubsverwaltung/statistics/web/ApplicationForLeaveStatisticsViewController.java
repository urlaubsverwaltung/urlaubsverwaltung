package org.synyx.urlaubsverwaltung.statistics.web;

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
import org.springframework.web.util.UriComponentsBuilder;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller to generate applications for leave statistics.
 */
@Controller
@RequestMapping(ApplicationForLeaveStatisticsViewController.STATISTICS_REL)
public class ApplicationForLeaveStatisticsViewController {

    static final String STATISTICS_REL = "/web/application/statistics";

    private final ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;
    private final ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService;
    private final VacationTypeService vacationTypeService;

    @Autowired
    public ApplicationForLeaveStatisticsViewController(ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService, ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService, VacationTypeService vacationTypeService) {
        this.applicationForLeaveStatisticsService = applicationForLeaveStatisticsService;
        this.applicationForLeaveStatisticsCsvExportService = applicationForLeaveStatisticsCsvExportService;
        this.vacationTypeService = vacationTypeService;
    }

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @PostMapping
    public String applicationForLeaveStatistics(@ModelAttribute("period") FilterPeriod period) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("redirect:" + STATISTICS_REL)
            .queryParam("from", period.getStartDateAsString())
            .queryParam("to", period.getEndDateAsString());
        return builder.toUriString();
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @GetMapping
    public String applicationForLeaveStatistics(@RequestParam(value = "from") String from, @RequestParam(value = "to") String to,
                                                Model model) {

        FilterPeriod period = new FilterPeriod(from, to);

        // NOTE: Not supported at the moment
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            model.addAttribute("period", period);
            model.addAttribute("errors", "INVALID_PERIOD");

            return "application/app_statistics";
        }

        List<ApplicationForLeaveStatistics> statistics = applicationForLeaveStatisticsService.getStatistics(period);

        model.addAttribute("from", period.getStartDate());
        model.addAttribute("to", period.getEndDate());
        model.addAttribute("statistics", statistics);
        model.addAttribute("period", period);
        model.addAttribute("vacationTypes", vacationTypeService.getVacationTypes());

        return "application/app_statistics";
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @GetMapping(value = "/download")
    public String downloadCSV(@RequestParam(value = "from", required = false) String from,
                              @RequestParam(value = "to", required = false) String to,
                              HttpServletResponse response,
                              Model model)
        throws IOException {

        FilterPeriod period = new FilterPeriod(from, to);

        // NOTE: Not supported at the moment
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            model.addAttribute("period", period);
            model.addAttribute("errors", "INVALID_PERIOD");

            return "application/app_statistics";
        }

        List<ApplicationForLeaveStatistics> statistics = applicationForLeaveStatisticsService.getStatistics(period);

        String fileName = applicationForLeaveStatisticsCsvExportService.getFileName(period);
        response.setContentType("text/csv");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        try (CSVWriter csvWriter = new CSVWriter(response.getWriter())) {
            applicationForLeaveStatisticsCsvExportService.writeStatistics(period, statistics, csvWriter);
        }

        model.addAttribute("period", period);

        return "application/app_statistics";
    }
}
