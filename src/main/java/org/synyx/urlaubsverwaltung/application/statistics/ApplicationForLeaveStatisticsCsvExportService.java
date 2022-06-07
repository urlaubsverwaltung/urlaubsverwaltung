package org.synyx.urlaubsverwaltung.application.statistics;

import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;

@Service
class ApplicationForLeaveStatisticsCsvExportService {

    private static final Locale LOCALE = Locale.GERMAN;
    private static final String DATE_FORMAT = "ddMMyyyy";

    private final MessageSource messageSource;
    private final VacationTypeService vacationTypeService;
    private final DateFormatAware dateFormatAware;

    @Autowired
    ApplicationForLeaveStatisticsCsvExportService(MessageSource messageSource, VacationTypeService vacationTypeService, DateFormatAware dateFormatAware) {
        this.messageSource = messageSource;
        this.vacationTypeService = vacationTypeService;
        this.dateFormatAware = dateFormatAware;
    }

    void writeStatistics(FilterPeriod period, List<ApplicationForLeaveStatistics> statistics, CSVWriter csvWriter) {
        final String[] csvHeader = {
            getTranslation("person.account.basedata.personnelNumber"),
            getTranslation("person.data.firstName"),
            getTranslation("person.data.lastName"),
            "",
            getTranslation("applications.statistics.allowed"),
            getTranslation("applications.statistics.waiting"),
            getTranslation("applications.statistics.left") + " (" + period.getStartDate().getYear() + ")",
            "",
            getTranslation("person.account.basedata.additionalInformation")
        };
        final String[] csvSubHeader = {
            "",
            "",
            "",
            "",
            "",
            "",
            getTranslation("duration.vacationDays"),
            getTranslation("duration.overtime")
        };

        final String startDateString = dateFormatAware.format(period.getStartDate());
        final String endDateString = dateFormatAware.format(period.getEndDate());
        final String headerNote = getTranslation("absence.period") + ": " + startDateString + " - " + endDateString;

        final DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(LOCALE);
        newSymbols.setDecimalSeparator(',');
        newSymbols.setGroupingSeparator('.');

        final DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(LOCALE);
        decimalFormat.setDecimalFormatSymbols(newSymbols);

        csvWriter.writeNext(new String[]{headerNote});
        csvWriter.writeNext(csvHeader);
        csvWriter.writeNext(csvSubHeader);

        final List<VacationType> allVacationTypes = vacationTypeService.getAllVacationTypes();

        final String translatedTextTotal = getTranslation("applications.statistics.total");
        for (ApplicationForLeaveStatistics applicationForLeaveStatistics : statistics) {

            final String[] csvRow = new String[csvHeader.length];
            csvRow[0] = applicationForLeaveStatistics.getPersonBasedata().map(PersonBasedata::getPersonnelNumber).orElse("");
            csvRow[1] = applicationForLeaveStatistics.getPerson().getFirstName();
            csvRow[2] = applicationForLeaveStatistics.getPerson().getLastName();
            csvRow[3] = translatedTextTotal;
            csvRow[4] = decimalFormat.format(applicationForLeaveStatistics.getTotalAllowedVacationDays());
            csvRow[5] = decimalFormat.format(applicationForLeaveStatistics.getTotalWaitingVacationDays());
            csvRow[6] = decimalFormat.format(applicationForLeaveStatistics.getLeftVacationDays());
            csvRow[7] = decimalFormat.format(BigDecimal.valueOf((double) applicationForLeaveStatistics.getLeftOvertime().toMinutes() / 60));
            csvRow[8] = applicationForLeaveStatistics.getPersonBasedata().map(PersonBasedata::getAdditionalInformation).orElse("");
            csvWriter.writeNext(csvRow);

            for (VacationType type : allVacationTypes) {
                if (applicationForLeaveStatistics.hasVacationType(type)) {
                    final String[] csvRowVacationTypes = new String[csvHeader.length];
                    csvRowVacationTypes[4] = getTranslation(type.getMessageKey());
                    csvRowVacationTypes[5] = decimalFormat.format(applicationForLeaveStatistics.getAllowedVacationDays(type));
                    csvRowVacationTypes[6] = decimalFormat.format(applicationForLeaveStatistics.getWaitingVacationDays(type));
                    csvWriter.writeNext(csvRowVacationTypes);
                }
            }
        }
    }

    String getFileName(FilterPeriod period) {
        return format("%s_%s_%s.csv", getTranslation("applications.statistics"),
            period.getStartDate().format(ofPattern(DATE_FORMAT)),
            period.getEndDate().format(ofPattern(DATE_FORMAT)));
    }

    private String getTranslation(String key, Object... args) {
        return messageSource.getMessage(key, args, LOCALE);
    }
}
