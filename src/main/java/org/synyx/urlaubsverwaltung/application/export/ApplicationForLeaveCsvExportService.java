package org.synyx.urlaubsverwaltung.application.export;


import com.opencsv.CSVWriter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.csv.CsvExportService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static java.text.NumberFormat.getInstance;
import static java.time.format.DateTimeFormatter.ofLocalizedDate;
import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;

@Service
class ApplicationForLeaveCsvExportService implements CsvExportService<ApplicationForLeaveExport> {

    private final MessageSource messageSource;

    ApplicationForLeaveCsvExportService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String fileName(FilterPeriod period, Locale locale) {
        final DateTimeFormatter dateTimeFormatter = ofLocalizedDate(SHORT).withLocale(locale);
        return format("%s_%s_%s_%s.csv",
            getTranslation(locale, "applications.export.filename").replace(" ", "-"),
            period.startDate().format(dateTimeFormatter).replace("/", "-"),
            period.endDate().format(dateTimeFormatter).replace("/", "-"),
            locale.getLanguage());
    }

    @Override
    public void write(FilterPeriod period, Locale locale, List<ApplicationForLeaveExport> applicationForLeaveExports, CSVWriter csvWriter) {

        final String[] csvHeader = {
            getTranslation(locale, "person.account.basedata.personnelNumber"),
            getTranslation(locale, "person.data.firstName"),
            getTranslation(locale, "person.data.lastName"),
            getTranslation(locale, "applications.export.departments"),
            getTranslation(locale, "applications.export.from"),
            getTranslation(locale, "applications.export.to"),
            getTranslation(locale, "applications.export.length"),
            getTranslation(locale, "applications.export.type"),
            getTranslation(locale, "applications.export.days"),
        };

        final DateTimeFormatter dateTimeFormatter = ofLocalizedDate(MEDIUM).withLocale(locale);
        final DecimalFormat decimalFormat = (DecimalFormat) getInstance(locale);
        csvWriter.writeNext(csvHeader);

        applicationForLeaveExports.forEach(applicationForLeaveExport ->
            applicationForLeaveExport.getApplicationForLeaves().forEach(applicationForLeave -> {
                final String[] applicationCsvRow = new String[csvHeader.length];
                applicationCsvRow[0] = applicationForLeaveExport.getPersonalNumber();
                applicationCsvRow[1] = applicationForLeaveExport.getFirstName();
                applicationCsvRow[2] = applicationForLeaveExport.getLastName();
                applicationCsvRow[3] = String.join(", ", applicationForLeaveExport.getDepartments());
                applicationCsvRow[4] = applicationForLeave.getStartDate().format(dateTimeFormatter);
                applicationCsvRow[5] = applicationForLeave.getEndDate().format(dateTimeFormatter);
                applicationCsvRow[6] = getTranslation(locale, applicationForLeave.getDayLength().name());
                applicationCsvRow[7] = applicationForLeave.getVacationType().getLabel(locale);
                applicationCsvRow[8] = decimalFormat.format(applicationForLeave.getWorkDays());

                csvWriter.writeNext(applicationCsvRow);
            })
        );
    }

    private String getTranslation(Locale locale, String key, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }
}
