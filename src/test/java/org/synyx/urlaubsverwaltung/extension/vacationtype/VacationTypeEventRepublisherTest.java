package org.synyx.urlaubsverwaltung.extension.vacationtype;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdatedEvent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacationTypeEventRepublisherTest {

    @Mock
    private VacationTypeService vacationTypeService;

    @Mock
    private VacationTypeEventHandlerExtension vacationTypeEventHandlerExtension;

    @Captor
    private ArgumentCaptor<VacationTypeUpdatedEvent> eventCaptor;

    private VacationTypeEventRepublisher vacationTypeEventRepublisher;

    @BeforeEach
    void setUp() {
        vacationTypeEventRepublisher = new VacationTypeEventRepublisher(vacationTypeService, vacationTypeEventHandlerExtension);
    }

    @Test
    void republishEvents() {
        VacationType<?> vacationType = mock(VacationType.class);
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        vacationTypeEventRepublisher.republishEvents();

        verify(vacationTypeService).getAllVacationTypes();

        verify(vacationTypeEventHandlerExtension).onVacationTypeUpdated(eventCaptor.capture());
        VacationTypeUpdatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.updatedVacationType()).isSameAs(vacationType);
    }
}
