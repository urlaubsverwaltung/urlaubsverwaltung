package org.synyx.urlaubsverwaltung.core.sync.providers.exchange;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.service.item.Appointment;

/**
 * Provides exchange specific objects.
 *
 * @author  Christian Lange - <lange@synyx.de>
 */
public class ExchangeFactory {

    public Appointment getNewAppointment(ExchangeService exchangeService) throws Exception {
        return new Appointment(exchangeService);
    }
}
