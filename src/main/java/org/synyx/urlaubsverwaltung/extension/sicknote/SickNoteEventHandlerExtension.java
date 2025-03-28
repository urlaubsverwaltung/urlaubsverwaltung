package org.synyx.urlaubsverwaltung.extension.sicknote;

import de.focus_shift.urlaubsverwaltung.extension.api.sicknote.DayLength;
import de.focus_shift.urlaubsverwaltung.extension.api.sicknote.SickNoteAcceptedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.sicknote.SickNoteCancelledEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.sicknote.SickNoteConvertedToApplicationEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.sicknote.SickNoteCreatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.sicknote.SickNotePeriodDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.sicknote.SickNotePersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.sicknote.SickNoteUpdatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteAcceptedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCancelledEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCreatedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteToApplicationConvertedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteUpdatedEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@ConditionalOnProperty(value = "uv.extensions.enabled", havingValue = "true")
public class SickNoteEventHandlerExtension {

    private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.systemDefault();

    private final TenantSupplier tenantSupplier;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SickNoteEventHandlerExtension(
        TenantSupplier tenantSupplier,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.tenantSupplier = tenantSupplier;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void on(SickNoteCancelledEvent event) {
        applicationEventPublisher.publishEvent(toSickNoteCancelledEventDTO(tenantSupplier.get(), event));
    }

    @EventListener
    void on(SickNoteCreatedEvent event) {
        applicationEventPublisher.publishEvent(toSickNoteCreatedEventDTO(tenantSupplier.get(), event));
    }

    @EventListener
    void on(SickNoteUpdatedEvent event) {
        applicationEventPublisher.publishEvent(toSickNoteUpdatedEventDTO(tenantSupplier.get(), event));
    }

    @EventListener
    void on(SickNoteAcceptedEvent event) {
        applicationEventPublisher.publishEvent(toSickNoteAcceptedEventDTO(tenantSupplier.get(), event));
    }

    @EventListener
    void on(SickNoteToApplicationConvertedEvent event) {
        applicationEventPublisher.publishEvent(toSickNoteConvertedEventDTO(tenantSupplier.get(), event));
    }

    private static SickNotePeriodDTO toPeriod(SickNote sickNote) {
        return SickNotePeriodDTO.builder()
            .startDate(localDateToInstant(sickNote.getStartDate()))
            .endDate(localDateToInstant(sickNote.getEndDate()))
            .dayLength(DayLength.valueOf(sickNote.getDayLength().name()))
            .build();
    }

    private static SickNotePeriodDTO toMedicalCertificatePeriod(SickNote sickNote) {
        if (sickNote.getAubStartDate() == null || sickNote.getAubEndDate() == null) {
            return null;
        }

        return SickNotePeriodDTO.builder()
            .startDate(localDateToInstant(sickNote.getAubStartDate()))
            .endDate(localDateToInstant(sickNote.getAubEndDate()))
            .build();
    }

    private static SickNotePersonDTO toSickNotePersonDTO(Person person) {
        return SickNotePersonDTO.builder()
            .personId(person.getId())
            .username(person.getUsername())
            .build();
    }

    private static Instant localDateToInstant(LocalDate localDate) {
        return localDate.atStartOfDay(DEFAULT_TIME_ZONE).toInstant();
    }

    private static String toStatus(SickNote sickNote) {
        return sickNote.getStatus().name();
    }

    private static String toSickNoteType(SickNote sickNote) {
        return sickNote.getSickNoteType().getCategory().name();
    }

    private static SickNoteCancelledEventDTO toSickNoteCancelledEventDTO(String tenantId, SickNoteCancelledEvent event) {
        final SickNotePersonDTO person = toSickNotePersonDTO(event.sickNote().getPerson());
        final SickNotePersonDTO applier = event.sickNote().getApplier() != null ? toSickNotePersonDTO(event.sickNote().getApplier()) : null;
        final SickNotePeriodDTO period = toPeriod(event.sickNote());
        final SickNotePeriodDTO medicalCertificatePeriod = toMedicalCertificatePeriod(event.sickNote());

        return SickNoteCancelledEventDTO.builder()
            .id(event.id())
            .sourceId(event.sickNote().getId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .person(person)
            .applier(applier)
            .type(toSickNoteType(event.sickNote()))
            .status(toStatus(event.sickNote()))
            .period(period)
            .medicalCertificatePeriod(medicalCertificatePeriod)
            .build();
    }

    private static SickNoteCreatedEventDTO toSickNoteCreatedEventDTO(String tenantId, SickNoteCreatedEvent event) {
        final SickNotePersonDTO person = toSickNotePersonDTO(event.sickNote().getPerson());
        final SickNotePersonDTO applier = event.sickNote().getApplier() != null ? toSickNotePersonDTO(event.sickNote().getApplier()) : null;
        final SickNotePeriodDTO period = toPeriod(event.sickNote());
        final SickNotePeriodDTO medicalCertificatePeriod = toMedicalCertificatePeriod(event.sickNote());

        return SickNoteCreatedEventDTO.builder()
            .id(event.id())
            .sourceId(event.sickNote().getId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .person(person)
            .applier(applier)
            .type(toSickNoteType(event.sickNote()))
            .status(toStatus(event.sickNote()))
            .period(period)
            .medicalCertificatePeriod(medicalCertificatePeriod)
            .build();
    }

    private static SickNoteUpdatedEventDTO toSickNoteUpdatedEventDTO(String tenantId, SickNoteUpdatedEvent event) {
        final SickNotePersonDTO person = toSickNotePersonDTO(event.sickNote().getPerson());
        final SickNotePersonDTO applier = event.sickNote().getApplier() != null ? toSickNotePersonDTO(event.sickNote().getApplier()) : null;
        final SickNotePeriodDTO period = toPeriod(event.sickNote());
        final SickNotePeriodDTO medicalCertificatePeriod = toMedicalCertificatePeriod(event.sickNote());

        return SickNoteUpdatedEventDTO.builder()
            .id(event.id())
            .sourceId(event.sickNote().getId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .person(person)
            .applier(applier)
            .type(toSickNoteType(event.sickNote()))
            .status(toStatus(event.sickNote()))
            .period(period)
            .medicalCertificatePeriod(medicalCertificatePeriod)
            .build();
    }

    private static SickNoteAcceptedEventDTO toSickNoteAcceptedEventDTO(String tenantId, SickNoteAcceptedEvent event) {
        final SickNotePersonDTO person = toSickNotePersonDTO(event.sickNote().getPerson());
        final SickNotePersonDTO applier = event.sickNote().getApplier() != null ? toSickNotePersonDTO(event.sickNote().getApplier()) : null;
        final SickNotePeriodDTO period = toPeriod(event.sickNote());
        final SickNotePeriodDTO medicalCertificatePeriod = toMedicalCertificatePeriod(event.sickNote());

        return SickNoteAcceptedEventDTO.builder()
            .id(event.id())
            .sourceId(event.sickNote().getId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .person(person)
            .applier(applier)
            .type(toSickNoteType(event.sickNote()))
            .status(toStatus(event.sickNote()))
            .period(period)
            .medicalCertificatePeriod(medicalCertificatePeriod)
            .build();
    }

    private static SickNoteConvertedToApplicationEventDTO toSickNoteConvertedEventDTO(String tenantId, SickNoteToApplicationConvertedEvent event) {
        final SickNotePersonDTO person = SickNoteEventHandlerExtension.toSickNotePersonDTO(event.sickNote().getPerson());
        final SickNotePersonDTO applier = event.sickNote().getApplier() != null ? toSickNotePersonDTO(event.sickNote().getApplier()) : null;
        final SickNotePeriodDTO period = toPeriod(event.sickNote());
        final SickNotePeriodDTO medicalCertificatePeriod = toMedicalCertificatePeriod(event.sickNote());

        return SickNoteConvertedToApplicationEventDTO.builder()
            .id(event.id())
            .sourceId(event.sickNote().getId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .person(person)
            .applier(applier)
            .type(toSickNoteType(event.sickNote()))
            .status(toStatus(event.sickNote()))
            .period(period)
            .medicalCertificatePeriod(medicalCertificatePeriod)
            .build();
    }
}
