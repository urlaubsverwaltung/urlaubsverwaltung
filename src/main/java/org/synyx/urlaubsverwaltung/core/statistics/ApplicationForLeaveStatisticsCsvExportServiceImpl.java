package org.synyx.urlaubsverwaltung.core.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import liquibase.util.csv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics;

@Service
public class ApplicationForLeaveStatisticsCsvExportServiceImpl
                implements ApplicationForLeaveStatisticsCsvExportService {

    private static final Locale LOCALE = Locale.GERMAN;
    private static final String DATE_FORMAT = "ddMMyyyy";

     private final MessageSource messageSource;
     private final VacationTypeService vacationTypeService;

    @Autowired
    public ApplicationForLeaveStatisticsCsvExportServiceImpl(MessageSource messageSource, VacationTypeService vacationTypeService) {
        this.messageSource = messageSource;
    this.vacationTypeService = vacationTypeService;
    }

    @Override
    public void writeStatistics(final FilterPeriod period, final List<ApplicationForLeaveStatistics> statistics,
                    final CSVWriter csvWriter) {
        final String[] csvHeader = { getTranslation("person.data.firstName", "Vorname"),
                        getTranslation("person.data.lastName", "Nachname"), "",
                        getTranslation("applications.statistics.allowed", "genehmigt"),
                        getTranslation("applications.statistics.waiting", "noch nicht genehmigt"),
                        getTranslation("applications.statistics.left", "verbleibend") + " ("
                                        + period.getStartDate().getYear() + ")",
                        "", getTranslation("applications.statistics.entitlement", "Urlaubsanspruch") };

        final String[] csvSubHeader = { "", "", "", "", "", getTranslation("duration.vacationDays", "Urlaubstage"),
                        getTranslation("duration.overtime", "Überstunden") };

        String headerNote = getTranslation("absence.period", "Zeitraum") + ": " + period.getStartDateAsString() + " - "
                        + period.getEndDateAsString();

        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(LOCALE);
        DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(LOCALE);
        newSymbols.setDecimalSeparator(',');
        newSymbols.setGroupingSeparator('.');
        decimalFormat.setDecimalFormatSymbols(newSymbols);

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
            csvRow[7] = decimalFormat.format(applicationForLeaveStatistics.getEntitlementVacationDays());

            csvWriter.writeNext(csvRow);

            for (VacationType type : vacationTypeService.getVacationTypes()) {

                String[] csvRowVacationTypes = new String[csvHeader.length];

                csvRowVacationTypes[2] = getTranslation(type.getMessageKey());
                csvRowVacationTypes[3] = decimalFormat
                                .format(applicationForLeaveStatistics.getAllowedVacationDays().get(type));
                csvRowVacationTypes[4] = decimalFormat
                                .format(applicationForLeaveStatistics.getWaitingVacationDays().get(type));

                csvWriter.writeNext(csvRowVacationTypes);
            }
        }

    }

    @Override
    public String getFileName(final FilterPeriod period) {
        return String.format("%s_%s_%s.csv", getTranslation("applications.statistics", "Statistik"),
                        period.getStartDate().toString(DATE_FORMAT), period.getEndDate().toString(DATE_FORMAT));
    }

    private String getTranslation(final String key, final Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }
}
