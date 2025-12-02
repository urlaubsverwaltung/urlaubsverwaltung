package org.synyx.urlaubsverwaltung.extension.companyvacation;

import de.focus_shift.urlaubsverwaltung.extension.api.companyvacation.CompanyVacationDeletedEventDto;
import de.focus_shift.urlaubsverwaltung.extension.api.companyvacation.CompanyVacationPeriodDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.companyvacation.CompanyVacationPublishedEventDto;
import de.focus_shift.urlaubsverwaltung.extension.api.companyvacation.DayLength;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.companyvacation.CompanyVacationDeletedEvent;
import org.synyx.urlaubsverwaltung.companyvacation.CompanyVacationPublishedEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyVacationEventHandlerExtensionTest {

    @Mock
    private TenantSupplier tenantSupplier;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    private CompanyVacationEventHandlerExtension sut;

    @BeforeEach
    void setUp() {
        sut = new CompanyVacationEventHandlerExtension(tenantSupplier, applicationEventPublisher);
        Mockito.when(tenantSupplier.get()).thenReturn("default");
    }

    @ParameterizedTest
    @MethodSource("publishEvents")
    void publishesDtoOnCompanyVacationPublishedEvent(CompanyVacationPublishedEvent event, DayLength expectedDayLength, LocalDate expectedDate, String expectedSourceId) {
        sut.on(event);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(CompanyVacationPublishedEventDto.class);
        final CompanyVacationPublishedEventDto dto = (CompanyVacationPublishedEventDto) captor.getValue();

        assertThat(dto.tenantId()).isEqualTo("default");
        assertThat(dto.id()).isEqualTo(event.id());
        assertThat(dto.sourceId()).isEqualTo(expectedSourceId);
        assertThat(dto.createdAt()).isEqualTo(event.createdAt());

        final CompanyVacationPeriodDTO period = dto.period();
        assertThat(period.startDate()).isEqualTo(localDateToInstant(expectedDate));
        assertThat(period.endDate()).isEqualTo(localDateToInstant(expectedDate));
        assertThat(period.dayLength()).isEqualTo(expectedDayLength);
    }

    private static Stream<Arguments> publishEvents() {
        final int year = LocalDate.now().getYear();
        final UUID id1 = UUID.randomUUID();
        final Instant createdAt1 = Instant.now();
        final CompanyVacationPublishedEvent christmasEvent = new CompanyVacationPublishedEvent(
            "settings-christmas-eve",
            id1,
            createdAt1,
            org.synyx.urlaubsverwaltung.period.DayLength.NOON,
            LocalDate.of(year, 12, 24),
            LocalDate.of(year, 12, 24)
        );

        final UUID id2 = UUID.randomUUID();
        final Instant createdAt2 = Instant.now();
        final CompanyVacationPublishedEvent newYearsEvent = new CompanyVacationPublishedEvent(
            "settings-new-years-eve",
            id2,
            createdAt2,
            org.synyx.urlaubsverwaltung.period.DayLength.MORNING,
            LocalDate.of(year, 12, 31),
            LocalDate.of(year, 12, 31)
        );

        return Stream.of(
            Arguments.of(christmasEvent, DayLength.NOON, LocalDate.of(year, 12, 24), "settings-christmas-eve"),
            Arguments.of(newYearsEvent, DayLength.MORNING, LocalDate.of(year, 12, 31), "settings-new-years-eve")
        );
    }

    @ParameterizedTest
    @MethodSource("deleteEvents")
    void publishesDtoOnCompanyVacationDeletedEvent(CompanyVacationDeletedEvent event, String expectedSourceId) {
        sut.on(event);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(CompanyVacationDeletedEventDto.class);
        final CompanyVacationDeletedEventDto dto = (CompanyVacationDeletedEventDto) captor.getValue();

        assertThat(dto.tenantId()).isEqualTo("default");
        assertThat(dto.id()).isEqualTo(event.id());
        assertThat(dto.sourceId()).isEqualTo(expectedSourceId);
        assertThat(dto.deletedAt()).isEqualTo(event.deletedAt());
    }

    private static Stream<Arguments> deleteEvents() {
        final UUID id1 = UUID.randomUUID();
        final Instant deletedAt1 = Instant.now();
        final CompanyVacationDeletedEvent christmasDeleted = new CompanyVacationDeletedEvent(
            "settings-christmas-eve",
            id1,
            deletedAt1
        );

        final UUID id2 = UUID.randomUUID();
        final Instant deletedAt2 = Instant.now();
        final CompanyVacationDeletedEvent newYearsDeleted = new CompanyVacationDeletedEvent(
            "settings-new-years-eve",
            id2,
            deletedAt2
        );

        return Stream.of(
            Arguments.of(christmasDeleted, "settings-christmas-eve"),
            Arguments.of(newYearsDeleted, "settings-new-years-eve")
        );
    }

    private static Instant localDateToInstant(LocalDate localDate) {
        return localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
    }
}
