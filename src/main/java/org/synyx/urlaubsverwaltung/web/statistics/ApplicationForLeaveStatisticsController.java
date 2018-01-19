package org.synyx.urlaubsverwaltung.web.statistics;

import liquibase.util.csv.CSVWriter;
import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.statistics.ApplicationForLeaveStatisticsService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller to generate applications for leave statistics.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping(ApplicationForLeaveStatisticsController.STATISTICS_REL)
public class ApplicationForLeaveStatisticsController {

    private static final Locale LOCALE = Locale.GERMAN;
    static final String STATISTICS_REL = "/web/application/statistics";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;

    @Autowired
    private VacationTypeService vacationTypeService;

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
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
    public String applicationForLeaveStatistics(@RequestParam(value = "from", required = false) String from,
                                                @RequestParam(value = "to", required = false) String to,
                                                Model model) {

        FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

        // NOTE: Not supported at the moment
        if (period.getStartDate().getYear() != period.getEndDate().getYear()) {
            model.addAttribute("period", period);
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, "INVALID_PERIOD");

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

        FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

        DateMidnight fromDate = period.getStartDate();
        DateMidnight toDate = period.getEndDate();

        final String[] csvHeader = { getTranslation("person.data.firstName", "Vorname"),
                getTranslation("person.data.lastName", "Vachname"), "",
                getTranslation("applications.statistics.allowed", "genehmigt"),
                getTranslation("applications.statistics.waiting", "noch nicht genehmigt"),
                getTranslation("applications.statistics.left", "verbleibend") + " (" + fromDate.getYear() + ")", "" };

        final String[] csvSubHeader = { "", "", "", "", "", getTranslation("duration.vacationDays", "Urlaubstage"),
                getTranslation("duration.overtime", "Überstunden") };

        String fileName = getTranslation("applications.statistics", "Statistik") + "_"
            + period.getStartDate().toString("ddMMyyyy") + "_" + period.getEndDate().toString("ddMMyyyy") + ".csv";

        String headerNote = getTranslation("absence.period", "Zeitraum") + ": " + period.getStartDateAsString() + " - "
            + period.getEndDateAsString();

        // NOTE: Not supported at the moment
        if (fromDate.getYear() != toDate.getYear()) {
            model.addAttribute("period", period);
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, "INVALID_PERIOD");

            return "application/app_statistics";
        }

        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(LOCALE);
        DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(LOCALE);
        newSymbols.setDecimalSeparator(',');
        newSymbols.setGroupingSeparator('.');
        decimalFormat.setDecimalFormatSymbols(newSymbols);

        List<ApplicationForLeaveStatistics> statistics = applicationForLeaveStatisticsService.getStatistics(period);

        response.setContentType("text/csv");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        try (CSVWriter csvWriter = new CSVWriter(response.getWriter())) {

            csvWriter.writeNext(new String[] { headerNote });
            csvWriter.writeNext(csvHeader);
            csvWriter.writeNext(csvSubHeader);

            String translatedTextTotal = getTranslation("applications.statistics.total", "gesamt");

            for (ApplicationForLeaveStatistics applicationForLeaveStatistics : statistics) {

                String[] csvRow = new String[csvHeader.length];

                csvRow[0] = applicationForLeaveStatistics.getPerson().getFirstName();
                csvRow[1] = applicationForLeaveStatistics.getPerson().getLastName();
                csvRow[2] = translatedTextTotal;
                csvRow[3] = decimalFormat.format(applicationForLeaveStatistics.getTotalAllowedVacationDays());
                csvRow[4] = decimalFormat.format(applicationForLeaveStatistics.getTotalWaitingVacationDays());
                csvRow[5] = decimalFormat.format(applicationForLeaveStatistics.getLeftVacationDays());
                csvRow[6] = decimalFormat.format(applicationForLeaveStatistics.getLeftOvertime());

                csvWriter.writeNext(csvRow);

                for (VacationType type : vacationTypeService.getVacationTypes()) {

                    String[] csvRowVacationTypes = new String[csvHeader.length];

                    csvRowVacationTypes[2] = type.getDisplayName();
                    csvRowVacationTypes[3] = decimalFormat
                        .format(applicationForLeaveStatistics.getAllowedVacationDays().get(type));
                    csvRowVacationTypes[4] = decimalFormat
                        .format(applicationForLeaveStatistics.getWaitingVacationDays().get(type));

                    csvWriter.writeNext(csvRowVacationTypes);
                }
            }
        }

        return "application/app_statistics";
    }

    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }
}
