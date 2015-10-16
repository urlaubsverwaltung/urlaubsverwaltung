package org.synyx.urlaubsverwaltung.web;

/**
 * Thrown in case no result found for a certain ID.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public abstract class NoResultForIDFoundException extends Exception {

    public NoResultForIDFoundException(Integer id, String type) {

        super("No " + type + " found for ID = " + id);
    }


    public NoResultForIDFoundException(String identifier, String type) {

        super("No " + type + " found for identifier = " + identifier);
    }
}
