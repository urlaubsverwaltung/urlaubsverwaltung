package org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion.Exchange2010_SP2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeFactoryTest {

    @Mock
    private ExchangeService exchangeService;

    @Test
    void getNewAppointment() throws ExchangeServiceException, ServiceLocalException {

        when(exchangeService.getRequestedServerVersion()).thenReturn(Exchange2010_SP2);

        final ExchangeFactory sut = new ExchangeFactory();
        final Appointment newAppointment = sut.getNewAppointment(exchangeService);
        assertThat(newAppointment.isNew()).isTrue();

    }

    @Test
    void getNewAppointmentThrowsException() {
        final ExchangeFactory sut = new ExchangeFactory();
        assertThrows(ExchangeServiceException.class, () -> sut.getNewAppointment(exchangeService));
    }
}
