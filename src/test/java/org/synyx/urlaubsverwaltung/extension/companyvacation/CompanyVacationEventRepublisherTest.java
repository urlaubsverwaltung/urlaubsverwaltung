package org.synyx.urlaubsverwaltung.extension.companyvacation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.companyvacation.CompanyVacationService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyVacationEventRepublisherTest {

    private CompanyVacationEventRepublisher sut;

    @Mock
    private CompanyVacationService companyVacationService;

    @BeforeEach
    void setUp() {
        sut = new CompanyVacationEventRepublisher(companyVacationService);
    }

    @Test
    void republishEvents_publishesEventsBasedOnPublicHolidaySettings() {
        sut.republishEvents();
        verify(companyVacationService).publishCompanyEvents();
    }
}
