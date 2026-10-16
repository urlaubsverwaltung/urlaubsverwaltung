package org.synyx.urlaubsverwaltung.extension.application;

import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationCreatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationPeriodDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationPersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.DayLength;
import de.focus_shift.urlaubsverwaltung.extension.api.application.VacationTypeDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAppliedEvent;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static java.time.Month.OCTOBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

@ExtendWith(MockitoExtension.class)
class ApplicationEventHandlerExtensionTest {

    @Mock
    private TenantSupplier tenantSupplier;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ApplicationEventHandlerExtension sut;

    @Nested
    class ApplicationAppliedEventTest {

        @Captor
        private ArgumentCaptor<ApplicationCreatedEventDTO> argumentCaptor;

        @Test
        void ensureEventIsConvertedToApplicationCreatedEventDTO() {

            final Person person = createPerson("muster");
            person.setId(1L);
            final Person applier = createPerson("office");
            applier.setId(2L);

            final VacationType<?> vacationType = createVacationType(10L, HOLIDAY, new StaticMessageSource());
            final LocalDate startDate = LocalDate.of(2026, OCTOBER, 19);
            final LocalDate endDate = LocalDate.of(2026, OCTOBER, 23);

            final Application application = createApplication(person, vacationType, startDate, endDate, FULL);
            application.setId(42L);
            application.setApplier(applier);
            application.setReason("Herbstferien");
            application.setTwoStageApproval(true);
            application.setTeamInformed(true);
            application.setHours(Duration.ofHours(8));

            final UUID eventId = UUID.randomUUID();
            final Instant createdAt = Instant.parse("2026-10-16T08:00:00Z");
            final ApplicationAppliedEvent event = new ApplicationAppliedEvent(eventId, createdAt, application);

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(event);

            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

            final ApplicationCreatedEventDTO result = argumentCaptor.getValue();
            assertThat(result.id()).isEqualTo(eventId);
            assertThat(result.sourceId()).isEqualTo(42L);
            assertThat(result.createdAt()).isEqualTo(createdAt);
            assertThat(result.tenantId()).isEqualTo("default");
            assertThat(result.person()).isEqualTo(ApplicationPersonDTO.builder().personId(1L).username("muster").build());
            assertThat(result.appliedBy()).isEqualTo(ApplicationPersonDTO.builder().personId(2L).username("office").build());
            assertThat(result.twoStageApproval()).isTrue();
            assertThat(result.period()).isEqualTo(ApplicationPeriodDTO.builder()
                .startDate(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .endDate(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .dayLength(DayLength.FULL)
                .build());
            assertThat(result.vacationType()).isEqualTo(VacationTypeDTO.builder()
                .sourceId(10L)
                .category("HOLIDAY")
                .requiresApprovalToApply(false)
                .requiresApprovalToCancel(false)
                .color("ORANGE")
                .visibleToEveryone(false)
                .translationKey("application.data.vacationType.holiday")
                .build());
            assertThat(result.reason()).isEqualTo("Herbstferien");
            assertThat(result.status()).isEqualTo("WAITING");
            assertThat(result.teamInformed()).isTrue();
            assertThat(result.hours()).isEqualTo(Duration.ofHours(8));
        }

        @Test
        void ensureAppliedByIsNullWithoutApplier() {

            final Person person = createPerson("muster");
            person.setId(1L);

            final VacationType<?> vacationType = createVacationType(10L, HOLIDAY, new StaticMessageSource());
            final Application application = createApplication(person, vacationType, LocalDate.of(2026, OCTOBER, 19), LocalDate.of(2026, OCTOBER, 19), FULL);
            application.setId(42L);

            when(tenantSupplier.get()).thenReturn("default");

            sut.on(ApplicationAppliedEvent.of(application));

            verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
            assertThat(argumentCaptor.getValue().appliedBy()).isNull();
        }
    }
}
