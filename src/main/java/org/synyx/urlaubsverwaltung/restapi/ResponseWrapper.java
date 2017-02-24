package org.synyx.urlaubsverwaltung.restapi;

/**
 * REST-API Wrapper class. Exists for historical reasons. It simulates a @ModelAttribute("response").
 *
 * @author  David Schilling - schilling@synyx.de
 */
public class ResponseWrapper<T> {

    private final T response;

    public ResponseWrapper(T response) {

        this.response = response;
    }

    public T getResponse() {

        return response;
    }
}
