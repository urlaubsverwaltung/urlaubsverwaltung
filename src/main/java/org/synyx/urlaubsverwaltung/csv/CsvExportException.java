package org.synyx.urlaubsverwaltung.csv;

public class CsvExportException extends RuntimeException {
    CsvExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
