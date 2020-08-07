package org.synyx.urlaubsverwaltung.statistics.web;

import liquibase.util.csv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
class ApplicationForLeaveStatisticsCsvExportServiceImpl implements ApplicationForLeaveStatisticsCsvExportService {

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
    public void writeStatistics(FilterPeriod period, List<ApplicationForLeaveStatistics> statistics, CSVWriter csvWriter) {
        final String[] csvHeader = {getTranslation("person.data.firstName", "Vorname"),
            getTranslation("person.data.lastName", "Nachname"), "",
            getTranslation("applications.statistics.allowed", "genehmigt"),
            getTranslation("applications.statistics.waiting", "noch nicht genehmigt"),
            getTranslation("applications.statistics.left", "verbleibend") + " (" + Year.from(period.getStartDate()).getValue() + ")", ""};

        final String[] csvSubHeader = {"", "", "", "", "", getTranslation("duration.vacationDays", "Urlaubstage"),
            getTranslation("duration.overtime", "Überstunden")};

        String headerNote = getTranslation("absence.period", "Zeitraum") + ": " + period.getStartDateAsString() + " - "
            + period.getEndDateAsString();

        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(LOCALE);
        DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(LOCALE);
        newSymbols.setDecimalSeparator(',');
        newSymbols.setGroupingSeparator('.');
        decimalFormat.setDecimalFormatSymbols(newSymbols);

        csvWriter.writeNext(new String[]{headerNote});
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
    public String getFileName(FilterPeriod period) {
        return String.format("%s_%s_%s.csv",
            getTranslation("applications.statistics", "Statistik"),
            DateTimeFormatter.ofPattern(DATE_FORMAT).format(period.getStartDate()),
            DateTimeFormatter.ofPattern(DATE_FORMAT).format(period.getEndDate()));
    }

    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }
}
