package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import org.synyx.urlaubsverwaltung.web.AbstractNoResultFoundException;

/**
 * Thrown in case no blackout period found for a certain ID.
 */
public class UnknownBlackoutPeriodException extends AbstractNoResultFoundException {
    public UnknownBlackoutPeriodException(Long id) {
        super(id, "blackoutperiod");
    }
}
