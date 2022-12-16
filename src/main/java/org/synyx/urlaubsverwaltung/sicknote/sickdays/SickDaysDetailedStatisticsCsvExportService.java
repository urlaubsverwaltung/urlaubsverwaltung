package org.synyx.urlaubsverwaltung.sicknote.sickdays;


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
class SickDaysDetailedStatisticsCsvExportService implements CsvExportService<SickDaysDetailedStatistics> {

    private static final Locale LOCALE = Locale.GERMAN;
    private static final String DATE_FORMAT = "ddMMyyyy";

    private final MessageSource messageSource;
    private final DateFormatAware dateFormatAware;

    SickDaysDetailedStatisticsCsvExportService(MessageSource messageSource, DateFormatAware dateFormatAware) {
        this.messageSource = messageSource;
        this.dateFormatAware = dateFormatAware;
    }

    @Override
    public String fileName(FilterPeriod period) {
        return format("%s_%s_%s.csv", getTranslation("sicknotes.statistics"),
            period.getStartDate().format(ofPattern(DATE_FORMAT)),
            period.getEndDate().format(ofPattern(DATE_FORMAT)));
    }

    @Override
    public void write(FilterPeriod period, List<SickDaysDetailedStatistics> allDetailedSickNotes, CSVWriter csvWriter) {
        final String[] csvHeader = {
            getTranslation("person.account.basedata.personnelNumber"),
            getTranslation("person.data.firstName"),
            getTranslation("person.data.lastName"),
            getTranslation("sicknotes.statistics.departments"),
            getTranslation("sicknotes.statistics.from"),
            getTranslation("sicknotes.statistics.to"),
            getTranslation("sicknotes.statistics.length"),
            getTranslation("sicknotes.statistics.days"),
            getTranslation("sicknotes.statistics.type"),
            getTranslation("sicknotes.statistics.certificate.from"),
            getTranslation("sicknotes.statistics.certificate.to"),
            getTranslation("sicknotes.statistics.certificate.days")
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

        allDetailedSickNotes.forEach(detailedSickNote ->
            detailedSickNote.getSickNotes().forEach(sickNote -> {
                final String[] sickNoteCsvRow = new String[csvHeader.length];
                sickNoteCsvRow[0] = detailedSickNote.getPersonalNumber();
                sickNoteCsvRow[1] = detailedSickNote.getPerson().getFirstName();
                sickNoteCsvRow[2] = detailedSickNote.getPerson().getLastName();
                sickNoteCsvRow[3] = String.join(", ", detailedSickNote.getDepartments());
                sickNoteCsvRow[4] = dateFormatAware.format(sickNote.getStartDate());
                sickNoteCsvRow[5] = dateFormatAware.format(sickNote.getEndDate());
                sickNoteCsvRow[6] = getTranslation(sickNote.getDayLength().name());
                sickNoteCsvRow[7] = decimalFormat.format(sickNote.getWorkDays());
                sickNoteCsvRow[8] = getTranslation(sickNote.getSickNoteType().getMessageKey());
                if (sickNote.isAubPresent()) {
                    sickNoteCsvRow[9] = dateFormatAware.format(sickNote.getAubStartDate());
                    sickNoteCsvRow[10] = dateFormatAware.format(sickNote.getAubEndDate());
                    sickNoteCsvRow[11] = decimalFormat.format(sickNote.getWorkDaysWithAub());
                }
                csvWriter.writeNext(sickNoteCsvRow);
            })
        );
    }

    private String getTranslation(String key, Object... args) {
        return messageSource.getMessage(key, args, LOCALE);
    }
}
