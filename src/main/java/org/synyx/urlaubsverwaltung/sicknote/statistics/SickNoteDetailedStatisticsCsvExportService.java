package org.synyx.urlaubsverwaltung.sicknote.statistics;

import liquibase.util.csv.CSVWriter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;

@Component
public class SickNoteDetailedStatisticsCsvExportService {

    private static final Locale LOCALE = Locale.GERMAN;
    private static final String DATE_FORMAT = "ddMMyyyy";

    private final MessageSource messageSource;
    private final DateFormatAware dateFormatAware;

    public SickNoteDetailedStatisticsCsvExportService(MessageSource messageSource,
                                                      DateFormatAware dateFormatAware) {
        this.messageSource = messageSource;
        this.dateFormatAware = dateFormatAware;
    }
    public String getFileName(FilterPeriod period) {
        return format("%s_%s_%s.csv", getTranslation("sicknote.statistics"),
            period.getStartDate().format(ofPattern(DATE_FORMAT)),
            period.getEndDate().format(ofPattern(DATE_FORMAT)));
    }

    void writeStatistics(FilterPeriod period, List<SickNoteDetailedStatistics> allDetailedSicknotes, CSVWriter csvWriter) {
        final String[] csvHeader = {
            getTranslation("person.account.basedata.personnelNumber"),
            getTranslation("person.data.firstName"),
            getTranslation("person.data.lastName"),
            getTranslation("sicknote.statistics.from"),
            getTranslation("sicknote.statistics.to"),
            getTranslation("sicknote.statistics.type"),
            getTranslation("sicknote.statistics.certificate")
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

        allDetailedSicknotes.forEach(detailedSickNote -> {

            final String[] personCsvRow = new String[csvHeader.length];
            personCsvRow[0] = detailedSickNote.getPersonalNumber();
            personCsvRow[1] = detailedSickNote.getFirstName();
            personCsvRow[2] = detailedSickNote.getLastName();
            csvWriter.writeNext(personCsvRow);

            detailedSickNote.getSickNotes().forEach(sickNote -> {
                final String[] sickNoteCsvRow = new String[csvHeader.length];
                sickNoteCsvRow[3] = dateFormatAware.format(sickNote.getStartDate());
                sickNoteCsvRow[4] = dateFormatAware.format(sickNote.getEndDate());
                sickNoteCsvRow[5] = getTranslation(sickNote.getSickNoteType().getMessageKey());
                sickNoteCsvRow[6] = setAub(sickNote);
                csvWriter.writeNext(sickNoteCsvRow);
            });
        });
    }

    private String setAub(SickNote sickNote) {
        return sickNote.isAubPresent() ?
            dateFormatAware.format(sickNote.getAubStartDate())
             + "-" + dateFormatAware.format(sickNote.getAubEndDate()) : "";
    }

    private String getTranslation(String key, Object... args) {
        return messageSource.getMessage(key, args, LOCALE);
    }
}
