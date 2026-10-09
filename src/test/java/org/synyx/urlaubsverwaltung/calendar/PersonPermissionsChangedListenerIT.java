package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonPermissionsChangedEvent;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@ContextConfiguration(classes = {PersonPermissionsChangedListener.class})
class PersonPermissionsChangedListenerIT extends SingleTenantTestContainersBase {

    @MockitoBean
    private CompanyCalendarService companyCalendarService;
    @MockitoBean
    private CalendarAccessibleService calendarAccessibleService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void ensureDeletedCompanyCalendarOnPersonPermissionsChangedEvent() {

        when(calendarAccessibleService.isCompanyCalendarAccessible()).thenReturn(false);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(42L);
        applicationEventPublisher.publishEvent(PersonPermissionsChangedEvent.of(person, List.of(USER, OFFICE), List.of(USER)));

        verify(companyCalendarService).deleteCalendarForPerson(42);
    }
}
