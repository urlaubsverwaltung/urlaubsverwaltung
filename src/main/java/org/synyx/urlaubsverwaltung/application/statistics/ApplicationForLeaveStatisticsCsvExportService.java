package org.synyx.urlaubsverwaltung.application.statistics;


import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.csv.CsvExportService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static java.text.NumberFormat.getInstance;
import static java.time.format.DateTimeFormatter.ofLocalizedDate;
import static java.time.format.FormatStyle.SHORT;

@Service
class ApplicationForLeaveStatisticsCsvExportService implements CsvExportService<ApplicationForLeaveStatistics> {

    private final MessageSource messageSource;
    private final VacationTypeService vacationTypeService;

    @Autowired
    ApplicationForLeaveStatisticsCsvExportService(MessageSource messageSource, VacationTypeService vacationTypeService) {
        this.messageSource = messageSource;
        this.vacationTypeService = vacationTypeService;
    }

    @Override
    public String fileName(FilterPeriod period, Locale locale) {
        final DateTimeFormatter dateTimeFormatter = ofLocalizedDate(SHORT).withLocale(locale);
        return format("%s_%s_%s_%s.csv",
            getTranslation(locale, "applications.statistics").replace(" ", "-"),
            period.startDate().format(dateTimeFormatter).replace("/", "-"),
            period.endDate().format(dateTimeFormatter).replace("/", "-"),
            locale.getLanguage());
    }

    @Override
    public void write(FilterPeriod period, Locale locale, List<ApplicationForLeaveStatistics> statistics, CSVWriter csvWriter) {

        final String[] csvHeader = {
            getTranslation(locale, "person.account.basedata.personnelNumber"),
            getTranslation(locale, "person.data.firstName"),
            getTranslation(locale, "person.data.lastName"),
            "",
            getTranslation(locale, "applications.statistics.allowed"),
            getTranslation(locale, "applications.statistics.waiting"),
            getTranslation(locale, "applications.statistics.left"),
            "",
            getTranslation(locale, "applications.statistics.left") + " (" + period.startDate().getYear() + ")",
            "",
            getTranslation(locale, "person.account.basedata.additionalInformation")
        };
        final String[] csvSubHeader = {
            "",
            "",
            "",
            "",
            "",
            "",
            getTranslation(locale, "duration.vacationDays"),
            getTranslation(locale, "duration.overtime"),
            getTranslation(locale, "duration.vacationDays"),
            getTranslation(locale, "duration.overtime")
        };

        final DecimalFormat decimalFormat = (DecimalFormat) getInstance(locale);

        csvWriter.writeNext(csvHeader);
        csvWriter.writeNext(csvSubHeader);

        final List<VacationType<?>> allVacationTypes = vacationTypeService.getAllVacationTypes();

        final String translatedTextTotal = getTranslation(locale, "applications.statistics.total");
        for (ApplicationForLeaveStatistics applicationForLeaveStatistics : statistics) {

            final String[] csvRow = new String[csvHeader.length];
            csvRow[0] = applicationForLeaveStatistics.getPersonBasedata().map(PersonBasedata::personnelNumber).orElse("");
            csvRow[1] = applicationForLeaveStatistics.getPerson().getFirstName();
            csvRow[2] = applicationForLeaveStatistics.getPerson().getLastName();
            csvRow[3] = translatedTextTotal;
            csvRow[4] = decimalFormat.format(applicationForLeaveStatistics.getTotalAllowedVacationDays());
            csvRow[5] = decimalFormat.format(applicationForLeaveStatistics.getTotalWaitingVacationDays());

            csvRow[6] = decimalFormat.format(applicationForLeaveStatistics.getLeftVacationDaysForPeriod());
            csvRow[7] = decimalFormat.format(BigDecimal.valueOf((double) applicationForLeaveStatistics.getLeftOvertimeForPeriod().toMinutes() / 60));

            csvRow[8] = decimalFormat.format(applicationForLeaveStatistics.getLeftVacationDaysForYear());
            csvRow[9] = decimalFormat.format(BigDecimal.valueOf((double) applicationForLeaveStatistics.getLeftOvertimeForYear().toMinutes() / 60));

            csvRow[10] = applicationForLeaveStatistics.getPersonBasedata().map(PersonBasedata::additionalInformation).orElse("");
            csvWriter.writeNext(csvRow);

            for (final VacationType<?> type : allVacationTypes) {
                if (applicationForLeaveStatistics.hasVacationType(type)) {
                    final String[] csvRowVacationTypes = new String[csvHeader.length];
                    csvRowVacationTypes[3] = type.getLabel(locale);
                    csvRowVacationTypes[4] = decimalFormat.format(applicationForLeaveStatistics.getAllowedVacationDays(type));
                    csvRowVacationTypes[5] = decimalFormat.format(applicationForLeaveStatistics.getWaitingVacationDays(type));
                    csvWriter.writeNext(csvRowVacationTypes);
                }
            }
        }
    }

    private String getTranslation(Locale locale, String key, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }
}
