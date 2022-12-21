package org.synyx.urlaubsverwaltung.application.export;


import com.opencsv.CSVWriter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.csv.CsvExportService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;

@Service
class ApplicationForLeaveCsvExportService implements CsvExportService<ApplicationForLeaveExport> {

    private static final Locale LOCALE = Locale.GERMAN;
    private static final String DATE_FORMAT = "ddMMyyyy";

    private final MessageSource messageSource;
    private final DateFormatAware dateFormatAware;

    ApplicationForLeaveCsvExportService(MessageSource messageSource, DateFormatAware dateFormatAware) {
        this.messageSource = messageSource;
        this.dateFormatAware = dateFormatAware;
    }

    @Override
    public String fileName(FilterPeriod period) {
        return format("%s_%s_%s.csv", getTranslation("applications.export"),
            period.getStartDate().format(ofPattern(DATE_FORMAT)),
            period.getEndDate().format(ofPattern(DATE_FORMAT)));
    }

    @Override
    public void write(FilterPeriod period, List<ApplicationForLeaveExport> applicationForLeaveExports, CSVWriter csvWriter) {
        final String[] csvHeader = {
            getTranslation("person.account.basedata.personnelNumber"),
            getTranslation("person.data.firstName"),
            getTranslation("person.data.lastName"),
            getTranslation("applications.export.departments"),
            getTranslation("applications.export.from"),
            getTranslation("applications.export.to"),
            getTranslation("applications.export.length"),
            getTranslation("applications.export.type"),
            getTranslation("applications.export.days"),
        };

        final DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(LOCALE);
        newSymbols.setDecimalSeparator(',');
        newSymbols.setGroupingSeparator('.');

        final DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(LOCALE);
        decimalFormat.setDecimalFormatSymbols(newSymbols);

        csvWriter.writeNext(csvHeader);

        applicationForLeaveExports.forEach(applicationForLeaveExport ->
            applicationForLeaveExport.getApplicationForLeaves().forEach(applicationForLeave -> {
                final String[] applicationCsvRow = new String[csvHeader.length];
                applicationCsvRow[0] = applicationForLeaveExport.getPersonalNumber();
                applicationCsvRow[1] = applicationForLeaveExport.getFirstName();
                applicationCsvRow[2] = applicationForLeaveExport.getLastName();
                applicationCsvRow[3] = String.join(", ", applicationForLeaveExport.getDepartments());
                applicationCsvRow[4] = dateFormatAware.format(applicationForLeave.getStartDate());
                applicationCsvRow[5] = dateFormatAware.format(applicationForLeave.getEndDate());
                applicationCsvRow[6] = getTranslation(applicationForLeave.getDayLength().name());
                applicationCsvRow[7] = getTranslation(applicationForLeave.getVacationType().getMessageKey());
                applicationCsvRow[8] = decimalFormat.format(applicationForLeave.getWorkDays());

                csvWriter.writeNext(applicationCsvRow);
            })
        );
    }

    private String getTranslation(String key, Object... args) {
        return messageSource.getMessage(key, args, LOCALE);
    }
}
