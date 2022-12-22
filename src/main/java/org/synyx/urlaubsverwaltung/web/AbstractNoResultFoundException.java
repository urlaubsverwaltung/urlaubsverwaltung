package org.synyx.urlaubsverwaltung.web;

/**
 * Thrown in case no result found for a certain ID.
 */
public abstract class AbstractNoResultFoundException extends Exception {

    protected AbstractNoResultFoundException(Long id, String type) {
        super("No " + type + " found for ID = " + id);
    }

    protected AbstractNoResultFoundException(String identifier, String type) {
        super("No " + type + " found for identifier = " + identifier);
    }
}
