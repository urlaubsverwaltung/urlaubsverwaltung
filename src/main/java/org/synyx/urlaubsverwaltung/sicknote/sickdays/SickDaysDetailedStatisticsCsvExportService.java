package org.synyx.urlaubsverwaltung.sicknote.sickdays;


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
class SickDaysDetailedStatisticsCsvExportService implements CsvExportService<SickDaysDetailedStatistics> {

    private final MessageSource messageSource;

    SickDaysDetailedStatisticsCsvExportService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String fileName(FilterPeriod period, Locale locale) {
        final DateTimeFormatter dateTimeFormatter = ofLocalizedDate(SHORT).withLocale(locale);
        return format("%s_%s_%s_%s.csv",
            getTranslation(locale, "action.sicknotes.download.filename").replace(" ", "-"),
            period.startDate().format(dateTimeFormatter).replace("/", "-"),
            period.endDate().format(dateTimeFormatter).replace("/", "-"),
            locale.getLanguage());
    }

    @Override
    public void write(FilterPeriod period, Locale locale, List<SickDaysDetailedStatistics> allDetailedSickNotes, CSVWriter csvWriter) {

        final String[] csvHeader = {
            getTranslation(locale, "person.account.basedata.personnelNumber"),
            getTranslation(locale, "person.data.firstName"),
            getTranslation(locale, "person.data.lastName"),
            getTranslation(locale, "sicknotes.statistics.departments"),
            getTranslation(locale, "sicknotes.statistics.from"),
            getTranslation(locale, "sicknotes.statistics.to"),
            getTranslation(locale, "sicknotes.statistics.length"),
            getTranslation(locale, "sicknotes.statistics.days"),
            getTranslation(locale, "sicknotes.statistics.type"),
            getTranslation(locale, "sicknotes.statistics.certificate.from"),
            getTranslation(locale, "sicknotes.statistics.certificate.to"),
            getTranslation(locale, "sicknotes.statistics.certificate.days")
        };

        final DateTimeFormatter dateTimeFormatter = ofLocalizedDate(MEDIUM).withLocale(locale);
        final DecimalFormat decimalFormat = (DecimalFormat) getInstance(locale);

        csvWriter.writeNext(csvHeader);

        allDetailedSickNotes.forEach(detailedSickNote ->
            detailedSickNote.getSickNotes().forEach(sickNote -> {
                final String[] sickNoteCsvRow = new String[csvHeader.length];
                sickNoteCsvRow[0] = detailedSickNote.getPersonalNumber();
                sickNoteCsvRow[1] = detailedSickNote.getPerson().getFirstName();
                sickNoteCsvRow[2] = detailedSickNote.getPerson().getLastName();
                sickNoteCsvRow[3] = String.join(", ", detailedSickNote.getDepartments());
                sickNoteCsvRow[4] = sickNote.getStartDate().format(dateTimeFormatter);
                sickNoteCsvRow[5] = sickNote.getEndDate().format(dateTimeFormatter);
                sickNoteCsvRow[6] = getTranslation(locale, sickNote.getDayLength().name());
                sickNoteCsvRow[7] = decimalFormat.format(sickNote.getWorkDays());
                sickNoteCsvRow[8] = getTranslation(locale, sickNote.getSickNoteType().getMessageKey());
                if (sickNote.isAubPresent()) {
                    sickNoteCsvRow[9] = sickNote.getAubStartDate().format(dateTimeFormatter);
                    sickNoteCsvRow[10] = sickNote.getAubEndDate().format(dateTimeFormatter);
                    sickNoteCsvRow[11] = decimalFormat.format(sickNote.getWorkDaysWithAub());
                }
                csvWriter.writeNext(sickNoteCsvRow);
            })
        );
    }

    private String getTranslation(Locale locale, String key, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }
}
