package org.synyx.urlaubsverwaltung.csv;


import com.opencsv.CSVWriter;
import net.fortuna.ical4j.validate.ValidationException;
import org.springframework.core.io.ByteArrayResource;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;

import static com.opencsv.ICSVWriter.DEFAULT_LINE_END;
import static com.opencsv.ICSVWriter.DEFAULT_QUOTE_CHARACTER;
import static com.opencsv.ICSVWriter.NO_QUOTE_CHARACTER;
import static java.nio.charset.StandardCharsets.UTF_8;

public interface CsvExportService<T> {

    /**
     * Writes the data and other information from the filter period into the csv writer
     *
     * @param period    to add period to csv
     * @param locale    for i18n (messages and number formats)
     * @param data      are the main information for the csv
     * @param csvWriter to write data that will be used to create the ByteArrayResource
     */
    void write(FilterPeriod period, Locale locale, List<T> data, CSVWriter csvWriter);

    /**
     * Contains the algorithm to create a unique filename
     *
     * @param period can be used for a unique filename
     * @param locale for i18n (messages and number formats)
     * @return the filename to be used for this kind of files
     */
    String fileName(FilterPeriod period, Locale locale);

    /**
     * Main method of this interface to retrieve the {@link CSVFile} containing the filename and resource.
     *
     * @param period will be used to create the content of the csv file
     * @param data   will be used to create the content of the csv file
     * @return a {@link CSVFile} containing the filename and resource
     */
    default CSVFile generateCSV(FilterPeriod period, Locale locale, List<T> data) {
        return new CSVFile(fileName(period, locale), resource(period, locale, data));
    }

    /**
     * Method to override the utf8 bom that is used at the start of the csv.
     *
     * @return a byte array with the bom
     */
    default byte[] bom() {
        return new byte[]{(byte) 239, (byte) 187, (byte) 191};
    }

    /**
     * Method to override the separator that is used to separate the column in a row
     *
     * @return a separator to separate columns of rows
     */
    default char separator() {
        return ';';
    }


    /**
     * Helper method to create a ByteArrayResource from the filter period and the provided data.
     *
     * @param period to create content
     * @param data   to create content
     * @return {@link ByteArrayResource} based on the filter period and data
     */
    default ByteArrayResource resource(FilterPeriod period, Locale locale, List<T> data) {
        final ByteArrayResource byteArrayResource;

        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byteArrayOutputStream.write(bom());

            try (final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream, UTF_8)) {
                try (final CSVWriter csvWriter = new CSVWriter(outputStreamWriter, separator(), NO_QUOTE_CHARACTER, DEFAULT_QUOTE_CHARACTER, DEFAULT_LINE_END)) {
                    write(period, locale, data, csvWriter);
                }
            }
            byteArrayResource = new ByteArrayResource(byteArrayOutputStream.toByteArray());
            return byteArrayResource;
        } catch (ValidationException | IOException e) {
            throw new CsvExportException("csv data not be written to ByteArrayResource", e);
        }
    }
}
