package org.synyx.urlaubsverwaltung.restapi;

import lombok.Value;

/**
 * REST-API Wrapper class. Exists for historical reasons. It simulates a @ModelAttribute("response").
 *
 * @author  David Schilling - schilling@synyx.de
 */
@Value
public class ResponseWrapper<T> {

    T response;
}
