package org.synyx.urlaubsverwaltung.companyvacation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForChristmasEveUpdatedEvent;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForNewYearsEveUpdatedEvent;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyVacationServiceTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    private CompanyVacationService sut;

    @BeforeEach
    void setUp() {
        sut = new CompanyVacationService(applicationEventPublisher);
    }

    @Test
    void publishesCompanyVacationForChristmasEveWhenWorkingDurationIsNotFull() {
        final WorkingDurationForChristmasEveUpdatedEvent event = new WorkingDurationForChristmasEveUpdatedEvent(DayLength.MORNING);

        sut.handleWorkingDurationForChristmasEveUpdatedEvent(event);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(CompanyVacationPublishedEvent.class);
        final CompanyVacationPublishedEvent publishedEvent = (CompanyVacationPublishedEvent) captor.getValue();

        // Inverse of MORNING -> NOON
        assertThat(publishedEvent.dayLength()).isEqualTo(DayLength.NOON);

        final int currentYear = LocalDate.now().getYear();
        assertThat(publishedEvent.startDate()).isEqualTo(LocalDate.of(currentYear, 12, 24));
        assertThat(publishedEvent.endDate()).isEqualTo(LocalDate.of(currentYear, 12, 24));
        assertThat(publishedEvent.sourceId()).isEqualTo("settings-christmas-eve");
    }

    @Test
    void deletesCompanyVacationForChristmasEveFullWorkingDay() {
        final WorkingDurationForChristmasEveUpdatedEvent event = new WorkingDurationForChristmasEveUpdatedEvent(DayLength.FULL);

        sut.handleWorkingDurationForChristmasEveUpdatedEvent(event);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(CompanyVacationDeletedEvent.class);
        final CompanyVacationDeletedEvent deletedEvent = (CompanyVacationDeletedEvent) captor.getValue();
        assertThat(deletedEvent.sourceId()).isEqualTo("settings-christmas-eve");
    }

    @Test
    void publishesCompanyVacationForNewYearsEveWhenWorkingDurationIsNotFull() {
        final WorkingDurationForNewYearsEveUpdatedEvent event = new WorkingDurationForNewYearsEveUpdatedEvent(DayLength.NOON);

        sut.handleWorkingDurationForNewYearsEveUpdatedEvent(event);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(CompanyVacationPublishedEvent.class);
        final CompanyVacationPublishedEvent publishedEvent = (CompanyVacationPublishedEvent) captor.getValue();

        // Inverse of NOON -> MORNING
        assertThat(publishedEvent.dayLength()).isEqualTo(DayLength.MORNING);

        final int currentYear = LocalDate.now().getYear();
        assertThat(publishedEvent.startDate()).isEqualTo(LocalDate.of(currentYear, 12, 31));
        assertThat(publishedEvent.endDate()).isEqualTo(LocalDate.of(currentYear, 12, 31));
        assertThat(publishedEvent.sourceId()).isEqualTo("settings-new-years-eve");
    }

    @Test
    void deletesCompanyVacationForNewYearsEveWhenFullWorkingDay() {
        final WorkingDurationForNewYearsEveUpdatedEvent event = new WorkingDurationForNewYearsEveUpdatedEvent(DayLength.FULL);

        sut.handleWorkingDurationForNewYearsEveUpdatedEvent(event);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(CompanyVacationDeletedEvent.class);
        final CompanyVacationDeletedEvent deletedEvent = (CompanyVacationDeletedEvent) captor.getValue();
        assertThat(deletedEvent.sourceId()).isEqualTo("settings-new-years-eve");
    }
}
