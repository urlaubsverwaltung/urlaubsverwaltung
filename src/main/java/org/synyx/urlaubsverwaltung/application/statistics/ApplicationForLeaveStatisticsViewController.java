package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.csv.CSVFile;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.application.statistics.ApplicationForLeaveStatisticsMapper.mapToApplicationForLeaveStatisticsDto;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

/**
 * Controller to generate applications for leave statistics.
 */
@Controller
@RequestMapping("/web/application/statistics")
class ApplicationForLeaveStatisticsViewController {

    private final ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;
    private final ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService;
    private final VacationTypeService vacationTypeService;
    private final DateFormatAware dateFormatAware;
    private final MessageSource messageSource;

    @Autowired
    ApplicationForLeaveStatisticsViewController(
        ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService,
        ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService,
        VacationTypeService vacationTypeService, DateFormatAware dateFormatAware, MessageSource messageSource) {

        this.applicationForLeaveStatisticsService = applicationForLeaveStatisticsService;
        this.applicationForLeaveStatisticsCsvExportService = applicationForLeaveStatisticsCsvExportService;
        this.vacationTypeService = vacationTypeService;
        this.dateFormatAware = dateFormatAware;
        this.messageSource = messageSource;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @PostMapping
    public String applicationForLeaveStatistics(@ModelAttribute("period") FilterPeriod period, Errors errors, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("filterPeriodIncorrect", true);
        }

        final String startDateIsoString = dateFormatAware.formatISO(period.getStartDate());
        final String endDateIsoString = dateFormatAware.formatISO(period.getEndDate());

        return "redirect:/web/application/statistics?from=" + startDateIsoString + "&to=" + endDateIsoString;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping
    public String applicationForLeaveStatistics(Locale locale,
                                                @RequestParam(value = "from", defaultValue = "") String from,
                                                @RequestParam(value = "to", defaultValue = "") String to, Model model) {

        final FilterPeriod period = toFilterPeriod(from, to);
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            model.addAttribute("period", period);
            model.addAttribute("errors", "INVALID_PERIOD");
            return "thymeleaf/application/application-statistics";
        }

        final List<ApplicationForLeaveStatisticsDto> statisticsDtos = applicationForLeaveStatisticsService.getStatistics(period).stream()
            .map(applicationForLeaveStatistics -> mapToApplicationForLeaveStatisticsDto(applicationForLeaveStatistics, locale, messageSource)).collect(toList());

        final boolean showPersonnelNumberColumn = statisticsDtos.stream()
            .anyMatch(statisticsDto -> hasText(statisticsDto.getPersonnelNumber()));

        model.addAttribute("period", period);
        model.addAttribute("from", period.getStartDate());
        model.addAttribute("to", period.getEndDate());
        model.addAttribute("statistics", statisticsDtos);
        model.addAttribute("showPersonnelNumberColumn", showPersonnelNumberColumn);
        model.addAttribute("vacationTypes", vacationTypeService.getAllVacationTypes());

        return "thymeleaf/application/application-statistics";
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping(value = "/download")
    public ResponseEntity<ByteArrayResource> downloadCSV(@RequestParam(value = "from", defaultValue = "") String from,
                                                         @RequestParam(value = "to", defaultValue = "") String to) {

        final FilterPeriod period = toFilterPeriod(from, to);

        // NOTE: Not supported at the moment
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            return ResponseEntity.badRequest().build();
        }

        final List<ApplicationForLeaveStatistics> statistics = applicationForLeaveStatisticsService.getStatistics(period);
        final CSVFile csvFile = applicationForLeaveStatisticsCsvExportService.generateCSV(period, statistics);

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
