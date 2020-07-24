package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarAccessibleServiceTest {

    private CalendarAccessibleService sut;

    @Mock
    private CompanyCalendarService companyCalendarService;

    @Mock
    private CompanyCalendarAccessibleRepository companyCalendarAccessibleRepository;

    @BeforeEach
    void setUp() {
        sut = new CalendarAccessibleService(companyCalendarService, companyCalendarAccessibleRepository);
    }

    @Test
    void isCompanyCalendarAccessibleHasNoEntity() {

        when(companyCalendarAccessibleRepository.findAll()).thenReturn(List.of());

        final boolean companyCalendarAccessible = sut.isCompanyCalendarAccessible();
        assertThat(companyCalendarAccessible).isFalse();
    }

    @Test
    void isCompanyCalendarAccessibleHasEntityWithAccessible() {

        final CompanyCalendarAccessible calendarAccessible = new CompanyCalendarAccessible();
        calendarAccessible.setAccessible(true);
        when(companyCalendarAccessibleRepository.findAll()).thenReturn(List.of(calendarAccessible));

        final boolean companyCalendarAccessible = sut.isCompanyCalendarAccessible();
        assertThat(companyCalendarAccessible).isTrue();
    }

    @Test
    void isCompanyCalendarAccessibleHasEntityWithNotAccessible() {

        final CompanyCalendarAccessible calendarAccessible = new CompanyCalendarAccessible();
        calendarAccessible.setAccessible(false);
        when(companyCalendarAccessibleRepository.findAll()).thenReturn(List.of(calendarAccessible));

        final boolean companyCalendarAccessible = sut.isCompanyCalendarAccessible();
        assertThat(companyCalendarAccessible).isFalse();
    }

    @Test
    void setCompanyCalendarAccessibilityToTrue() {

        final ArgumentCaptor<CompanyCalendarAccessible> accessibleCaptor = forClass(CompanyCalendarAccessible.class);
        when(companyCalendarAccessibleRepository.findAll()).thenReturn(List.of());

        sut.enableCompanyCalendar();

        verify(companyCalendarAccessibleRepository).save(accessibleCaptor.capture());
        assertThat(accessibleCaptor.getValue().isAccessible()).isTrue();
    }

    @Test
    void setCompanyCalendarAccessibilityToTrueAndWasFalse() {

        final ArgumentCaptor<CompanyCalendarAccessible> accessibleCaptor = forClass(CompanyCalendarAccessible.class);

        final CompanyCalendarAccessible companyCalendarAccessible = new CompanyCalendarAccessible();
        companyCalendarAccessible.setAccessible(false);
        when(companyCalendarAccessibleRepository.findAll()).thenReturn(List.of(companyCalendarAccessible));

        sut.enableCompanyCalendar();

        verify(companyCalendarAccessibleRepository).save(accessibleCaptor.capture());
        assertThat(accessibleCaptor.getValue().isAccessible()).isTrue();
    }

    @Test
    void ensureCalendarAccessibilityIsDisabled() {

        final ArgumentCaptor<CompanyCalendarAccessible> accessibleCaptor = forClass(CompanyCalendarAccessible.class);

        final CompanyCalendarAccessible companyCalendarAccessible = new CompanyCalendarAccessible();
        companyCalendarAccessible.setAccessible(true);
        when(companyCalendarAccessibleRepository.findAll()).thenReturn(List.of(companyCalendarAccessible));

        sut.disableCompanyCalendar();

        verify(companyCalendarAccessibleRepository).save(accessibleCaptor.capture());
        assertThat(accessibleCaptor.getValue().isAccessible()).isFalse();
    }

    @Test
    void ensureCalendarDeletionWhenAccessibilityIsDisabled() {

        sut.disableCompanyCalendar();

        verify(companyCalendarService).deleteCalendarsForPersonsWithoutOneOfRole(Role.BOSS, Role.OFFICE);
    }
}
