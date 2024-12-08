package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.PersonDisabledEvent;

import static org.mockito.Mockito.verify;

@SpringBootTest
@ContextConfiguration(classes = {PersonDisabledListener.class})
class PersonDisabledListenerIT extends SingleTenantTestContainersBase {

    @MockitoBean
    private PersonCalendarService personCalendarService;
    @MockitoBean
    private DepartmentCalendarService departmentCalendarService;
    @MockitoBean
    private CompanyCalendarService companyCalendarService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void ensureDeletedPersonalCalendarOnPersonDisabledEvent() {

        applicationEventPublisher.publishEvent(new PersonDisabledEvent(this, 42L, "niceName", "username", "email"));

        verify(personCalendarService).deletePersonalCalendarForPerson(42);
        verify(departmentCalendarService).deleteDepartmentsCalendarsForPerson(42);
        verify(companyCalendarService).deleteCalendarForPerson(42);
    }
}
