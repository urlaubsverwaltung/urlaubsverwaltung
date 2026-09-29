package org.synyx.urlaubsverwaltung.account;

import com.opencsv.CSVWriter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.csv.CsvExportService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofLocalizedDate;
import static java.time.format.FormatStyle.MEDIUM;

@Service
class RemainingVacationDaysCsvExportService implements CsvExportService<RemainingVacationDaysCsvRow> {

    private final MessageSource messageSource;

    RemainingVacationDaysCsvExportService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String fileName(FilterPeriod period, Locale locale) {
        return "%s_%d_%s.csv".formatted(
            getTranslation(locale, "account.remaining_vacation_days.export.filename").replace(" ", "-"),
            period.startDate().getYear(),
            locale.getLanguage());
    }

    @Override
    public void write(FilterPeriod period, Locale locale, List<RemainingVacationDaysCsvRow> rows, CSVWriter csvWriter) {

        csvWriter.writeNext(new String[]{
            getTranslation(locale, "person.account.basedata.personnelNumber"),
            getTranslation(locale, "person.data.firstName"),
            getTranslation(locale, "person.data.lastName"),
            getTranslation(locale, "applications.export.departments"),
            getTranslation(locale, "account.remaining_vacation_days.export.remainingVacationDays"),
            getTranslation(locale, "account.remaining_vacation_days.export.remainingVacationDaysNotExpiring"),
            getTranslation(locale, "account.remaining_vacation_days.export.expiryDate")
        });

        final NumberFormat numberFormat = NumberFormat.getInstance(locale);
        final DateTimeFormatter dateTimeFormatter = ofLocalizedDate(MEDIUM).withLocale(locale);

        for (RemainingVacationDaysCsvRow row : rows) {
            csvWriter.writeNext(new String[]{
                row.personnelNumber(),
                row.firstName(),
                row.lastName(),
                String.join(", ", row.departments()),
                numberFormat.format(row.remainingVacationDays()),
                numberFormat.format(row.remainingVacationDaysNotExpiring()),
                row.expiryDate() == null ? "" : row.expiryDate().format(dateTimeFormatter)
            });
        }
    }

    private String getTranslation(Locale locale, String key) {
        return messageSource.getMessage(key, new Object[]{}, locale);
    }
}
