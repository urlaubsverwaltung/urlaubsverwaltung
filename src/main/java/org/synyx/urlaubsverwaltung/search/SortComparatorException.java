package org.synyx.urlaubsverwaltung.search;

public class SortComparatorException extends RuntimeException {

    SortComparatorException(String message) {
        super(message);
    }

    SortComparatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
