package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import liquibase.util.csv.CSVWriter;

/**
 * Controller to generate applications for leave statistics.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
@RequestMapping("/web/application")
@Controller
public class ApplicationForLeaveStatisticsController {

    protected static final Locale LOCALE = Locale.GERMAN;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PersonService personService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private VacationTypeService vacationTypeService;

    @Autowired
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics", method = RequestMethod.POST)
    public String applicationForLeaveStatistics(@ModelAttribute("period") FilterPeriod period) {

        return "redirect:/web/application/statistics?from=" + period.getStartDateAsString() + "&to="
            + period.getEndDateAsString();
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public String applicationForLeaveStatistics(@RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to, Model model) {

        FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

        DateMidnight fromDate = period.getStartDate();
        DateMidnight toDate = period.getEndDate();

        // NOTE: Not supported at the moment
        if (fromDate.getYear() != toDate.getYear()) {
            model.addAttribute("period", period);
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, "INVALID_PERIOD");

            return "application/app_statistics";
        }

        List<Person> persons = getRelevantPersons();

        List<ApplicationForLeaveStatistics> statistics = persons.stream()
            .map(person -> applicationForLeaveStatisticsBuilder.build(person, fromDate, toDate))
            .collect(Collectors.toList());

        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("statistics", statistics);
        model.addAttribute("period", period);
        model.addAttribute("vacationTypes", vacationTypeService.getVacationTypes());

        return "application/app_statistics";
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics/download", method = RequestMethod.GET)
    public String downloadCSV(@RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to, HttpServletResponse response, Model model)
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

        List<Person> persons = getRelevantPersons();

        List<ApplicationForLeaveStatistics> statistics = persons.stream()
            .map(person -> applicationForLeaveStatisticsBuilder.build(person, fromDate, toDate))
            .collect(Collectors.toList());

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

    private List<Person> getRelevantPersons() {

        Person signedInUser = sessionService.getSignedInUser();

        if (signedInUser.hasRole(Role.DEPARTMENT_HEAD)) {
            return departmentService.getManagedMembersOfDepartmentHead(signedInUser);
        }

        return personService.getActivePersons();
    }

    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }
}
