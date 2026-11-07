package org.synyx.urlaubsverwaltung.companyvacation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosure;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureChangedEvent;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntry;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.MAY;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyVacationServiceImplTest {

    private static final LocalDate CHRISTMAS_EVE = LocalDate.of(2026, DECEMBER, 24);
    private static final LocalDate NEW_YEARS_EVE = LocalDate.of(2026, DECEMBER, 31);

    private CompanyVacationServiceImpl sut;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private CompanyClosureService companyClosureService;

    @BeforeEach
    void setUp() {
        sut = new CompanyVacationServiceImpl(companyClosureService, Clock.fixed(Instant.parse("2026-11-06T10:00:00Z"), UTC), applicationEventPublisher);
    }

    @Test
    void publishesFromCompanyClosuresOnChange() {
        when(companyClosureService.getCompanyClosures(CHRISTMAS_EVE, CHRISTMAS_EVE))
            .thenReturn(List.of(new CompanyClosure(CHRISTMAS_EVE, DayLength.NOON, "Heiligabend")));

        sut.handleCompanyClosureChangedEvent(new CompanyClosureChangedEvent(MonthDay.of(DECEMBER, 24)));

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOfSatisfying(CompanyVacationPublishedEvent.class, event -> {
            assertThat(event.dayLength()).isEqualTo(DayLength.NOON);
            assertThat(event.startDate()).isEqualTo(CHRISTMAS_EVE);
            assertThat(event.endDate()).isEqualTo(CHRISTMAS_EVE);
            assertThat(event.sourceId()).isEqualTo("settings-christmas-eve");
        });
    }

    @Test
    void deletesOnChangeWithoutClosure() {
        when(companyClosureService.getCompanyClosures(NEW_YEARS_EVE, NEW_YEARS_EVE)).thenReturn(List.of());

        sut.handleCompanyClosureChangedEvent(new CompanyClosureChangedEvent(MonthDay.of(DECEMBER, 31)));

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOfSatisfying(CompanyVacationDeletedEvent.class,
            event -> assertThat(event.sourceId()).isEqualTo("settings-new-years-eve"));
    }

    @Test
    void publishesForNewClosureDay() {
        final LocalDate day = LocalDate.of(2026, MAY, 15);
        when(companyClosureService.getCompanyClosures(day, day))
            .thenReturn(List.of(new CompanyClosure(day, DayLength.FULL, "Brueckentag")));

        sut.handleCompanyClosureChangedEvent(new CompanyClosureChangedEvent(MonthDay.of(MAY, 15)));

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOfSatisfying(CompanyVacationPublishedEvent.class, event -> {
            assertThat(event.sourceId()).isEqualTo("company-closure-05-15");
            assertThat(event.dayLength()).isEqualTo(DayLength.FULL);
            assertThat(event.startDate()).isEqualTo(day);
            assertThat(event.endDate()).isEqualTo(day);
        });
    }

    @Test
    void publishCompanyEventsCoversAllEntries() {
        final LocalDate day = LocalDate.of(2026, MAY, 15);
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(List.of(
            new CompanyClosureEntry(MonthDay.of(DECEMBER, 24), "Heiligabend", List.of()),
            new CompanyClosureEntry(MonthDay.of(DECEMBER, 31), "Silvester", List.of()),
            new CompanyClosureEntry(MonthDay.of(MAY, 15), "Brueckentag", List.of())));
        when(companyClosureService.getCompanyClosures(CHRISTMAS_EVE, CHRISTMAS_EVE)).thenReturn(List.of());
        when(companyClosureService.getCompanyClosures(NEW_YEARS_EVE, NEW_YEARS_EVE)).thenReturn(List.of());
        when(companyClosureService.getCompanyClosures(day, day)).thenReturn(List.of());

        sut.publishCompanyEvents();

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher, times(3)).publishEvent(captor.capture());
        assertThat(captor.getAllValues()).extracting(e -> ((CompanyVacationDeletedEvent) e).sourceId())
            .containsExactly("settings-christmas-eve", "settings-new-years-eve", "company-closure-05-15");
    }

    @Test
    void publishCompanyEventsPublishesBothDays() {
        when(companyClosureService.getCompanyClosureEntries()).thenReturn(List.of(
            new CompanyClosureEntry(MonthDay.of(DECEMBER, 24), "Heiligabend", List.of()),
            new CompanyClosureEntry(MonthDay.of(DECEMBER, 31), "Silvester", List.of())));
        when(companyClosureService.getCompanyClosures(CHRISTMAS_EVE, CHRISTMAS_EVE))
            .thenReturn(List.of(new CompanyClosure(CHRISTMAS_EVE, DayLength.NOON, "Heiligabend")));
        when(companyClosureService.getCompanyClosures(NEW_YEARS_EVE, NEW_YEARS_EVE)).thenReturn(List.of());

        sut.publishCompanyEvents();

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher, times(2)).publishEvent(captor.capture());

        assertThat(captor.getAllValues().get(0)).isInstanceOfSatisfying(CompanyVacationPublishedEvent.class,
            event -> assertThat(event.sourceId()).isEqualTo("settings-christmas-eve"));
        assertThat(captor.getAllValues().get(1)).isInstanceOfSatisfying(CompanyVacationDeletedEvent.class,
            event -> assertThat(event.sourceId()).isEqualTo("settings-new-years-eve"));
    }
}
