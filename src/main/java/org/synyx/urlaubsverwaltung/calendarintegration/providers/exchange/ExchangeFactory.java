package org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.service.item.Appointment;

/**
 * Provides exchange specific objects.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
class ExchangeFactory {

    Appointment getNewAppointment(ExchangeService exchangeService) throws ExchangeServiceException {
        try {

            return new Appointment(exchangeService);
        } catch (Exception e) {

            throw new ExchangeServiceException(e.getMessage());
        }
    }
}
