package org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceRequestException;
import microsoft.exchange.webservices.data.core.service.item.Item;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion.Exchange2010_SP2;
import static microsoft.exchange.webservices.data.core.enumeration.service.MessageDisposition.SaveOnly;
import static microsoft.exchange.webservices.data.core.enumeration.service.SendInvitationsMode.SendToNone;
import static microsoft.exchange.webservices.data.property.complex.FolderId.getFolderIdFromString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ExchangeCalendarDependenciesTest {

    @Test
    void checkDependenciesAvailable() {
        final ExchangeService exchangeService = new ExchangeService(Exchange2010_SP2);
        exchangeService.setUrl(URI.create("http://localhost"));
        assertThrows(ServiceRequestException.class,() -> exchangeService.createItem(mock(Item.class), getFolderIdFromString("folderId"), SaveOnly, SendToNone));
    }
}
