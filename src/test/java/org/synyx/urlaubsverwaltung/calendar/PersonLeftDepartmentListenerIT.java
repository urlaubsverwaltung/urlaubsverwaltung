package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.department.PersonLeftDepartmentEvent;

import static org.mockito.Mockito.verify;

@SpringBootTest
@ContextConfiguration(classes = {PersonLeftDepartmentListener.class})
class PersonLeftDepartmentListenerIT extends TestContainersBase {

    @MockitoBean
    private DepartmentCalendarService departmentCalendarService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void ensureDeletedCompanyCalendarOnPersonLeftDepartmentEvent() {

        applicationEventPublisher.publishEvent(new PersonLeftDepartmentEvent(this, 1, 42));
        verify(departmentCalendarService).deleteCalendarForDepartmentAndPerson(42, 1);
    }
}
