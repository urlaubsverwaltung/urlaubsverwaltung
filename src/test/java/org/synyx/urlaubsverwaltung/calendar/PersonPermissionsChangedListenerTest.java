package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonPermissionsChangedEvent;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonPermissionsChangedListenerTest {

    private PersonPermissionsChangedListener sut;

    @Mock
    private CompanyCalendarService companyCalendarService;
    @Mock
    private CalendarAccessibleService calendarAccessibleService;

    @BeforeEach
    void setUp() {
        sut = new PersonPermissionsChangedListener(companyCalendarService, calendarAccessibleService);
    }

    @Test
    void ensureDeletesCompanyCalendarWhenPersonIsNeitherBossNorOfficeAnymore() {

        when(calendarAccessibleService.isCompanyCalendarAccessible()).thenReturn(false);

        sut.handlePersonPermissionsChangedEvent(PersonPermissionsChangedEvent.of(person(), List.of(USER, OFFICE), List.of(USER)));

        verify(companyCalendarService).deleteCalendarForPerson(42);
    }

    @Test
    void ensureKeepsCompanyCalendarWhenPersonIsStillBoss() {

        sut.handlePersonPermissionsChangedEvent(PersonPermissionsChangedEvent.of(person(), List.of(USER, BOSS, OFFICE), List.of(USER, BOSS)));

        verifyNoInteractions(companyCalendarService);
    }

    @Test
    void ensureKeepsCompanyCalendarWhenPersonIsStillOffice() {

        sut.handlePersonPermissionsChangedEvent(PersonPermissionsChangedEvent.of(person(), List.of(USER, BOSS, OFFICE), List.of(USER, OFFICE)));

        verifyNoInteractions(companyCalendarService);
    }

    @Test
    void ensureKeepsCompanyCalendarWhenCompanyCalendarIsAccessibleForEveryone() {

        when(calendarAccessibleService.isCompanyCalendarAccessible()).thenReturn(true);

        sut.handlePersonPermissionsChangedEvent(PersonPermissionsChangedEvent.of(person(), List.of(USER, OFFICE), List.of(USER)));

        verifyNoInteractions(companyCalendarService);
    }

    private static Person person() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(42L);
        return person;
    }
}
