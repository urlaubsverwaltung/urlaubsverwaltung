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

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ofLocalizedDate;
import static java.time.format.FormatStyle.MEDIUM;

/**
 * CSV of the remaining vacation days that expired in a run of the expiry notification, attached to the mail to office.
 */
@Service
class ExpiredRemainingVacationDaysCsvExportService implements CsvExportService<ExpiredRemainingVacationDaysCsvRow> {

    private final MessageSource messageSource;

    ExpiredRemainingVacationDaysCsvExportService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String fileName(FilterPeriod period, Locale locale) {
        return "%s_%s_%s.csv".formatted(
            getTranslation(locale, "account.expired_remaining_vacation_days.export.filename").replace(" ", "-"),
            period.startDate().format(ISO_LOCAL_DATE),
            locale.getLanguage());
    }

    @Override
    public void write(FilterPeriod period, Locale locale, List<ExpiredRemainingVacationDaysCsvRow> rows, CSVWriter csvWriter) {

        csvWriter.writeNext(new String[]{
            getTranslation(locale, "person.account.basedata.personnelNumber"),
            getTranslation(locale, "person.data.firstName"),
            getTranslation(locale, "person.data.lastName"),
            getTranslation(locale, "applications.export.departments"),
            getTranslation(locale, "account.remaining_vacation_days.export.expiryDate"),
            getTranslation(locale, "account.expired_remaining_vacation_days.export.expiredRemainingVacationDays"),
            getTranslation(locale, "account.expired_remaining_vacation_days.export.remainingVacationDaysNotExpiring"),
            getTranslation(locale, "account.expired_remaining_vacation_days.export.vacationDaysLeft")
        });

        final NumberFormat numberFormat = NumberFormat.getInstance(locale);
        final DateTimeFormatter dateTimeFormatter = ofLocalizedDate(MEDIUM).withLocale(locale);

        for (ExpiredRemainingVacationDaysCsvRow row : rows) {
            csvWriter.writeNext(new String[]{
                row.personnelNumber(),
                row.firstName(),
                row.lastName(),
                String.join(", ", row.departments()),
                row.expiryDate().format(dateTimeFormatter),
                numberFormat.format(row.expiredRemainingVacationDays()),
                numberFormat.format(row.remainingVacationDaysNotExpiring()),
                numberFormat.format(row.vacationDaysLeft())
            });
        }
    }

    private String getTranslation(Locale locale, String key) {
        return messageSource.getMessage(key, new Object[]{}, locale);
    }
}
