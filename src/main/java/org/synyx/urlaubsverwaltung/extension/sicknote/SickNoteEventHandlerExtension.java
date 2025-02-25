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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.absence.AbsencePeriod;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Component
@ConditionalOnProperty(value = "uv.extensions.enabled", havingValue = "true")
public class SickNoteEventHandlerExtension {

    private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.systemDefault();

    private final TenantSupplier tenantSupplier;
    private final AbsenceService absenceService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SickNoteEventHandlerExtension(TenantSupplier tenantSupplier,
                                         AbsenceService absenceService,
                                         ApplicationEventPublisher applicationEventPublisher) {
        this.tenantSupplier = tenantSupplier;
        this.absenceService = absenceService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private static Set<LocalDate> toAbsentWorkingDays(AbsencePeriod absencePeriod) {
        return absencePeriod.absenceRecords()
            .stream()
            .map(AbsencePeriod.Record::getDate)
            .collect(Collectors.toSet());
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

    private static Function<AbsencePeriod, SickNoteCancelledEventDTO> toSickNoteCancelledEventDTO(String tenantId, SickNoteCancelledEvent event) {
        return absencePeriod -> {
            final SickNotePersonDTO person = toSickNotePersonDTO(event.sickNote().getPerson());
            final SickNotePersonDTO applier = event.sickNote().getApplier() != null ? toSickNotePersonDTO(event.sickNote().getApplier()) : null;
            final SickNotePeriodDTO period = toPeriod(event.sickNote());
            final SickNotePeriodDTO medicalCertificatePeriod = toMedicalCertificatePeriod(event.sickNote());
            final Set<LocalDate> absentWorkingDays = toAbsentWorkingDays(absencePeriod);

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
                .absentWorkingDays(absentWorkingDays)
                .build();

        };
    }

    private static Function<AbsencePeriod, SickNoteCreatedEventDTO> toSickNoteCreatedEventDTO(String tenantId, SickNoteCreatedEvent event) {
        return absencePeriod -> {
            final SickNotePersonDTO person = toSickNotePersonDTO(event.sickNote().getPerson());
            final SickNotePersonDTO applier = event.sickNote().getApplier() != null ? toSickNotePersonDTO(event.sickNote().getApplier()) : null;
            final SickNotePeriodDTO period = toPeriod(event.sickNote());
            final SickNotePeriodDTO medicalCertificatePeriod = toMedicalCertificatePeriod(event.sickNote());
            final Set<LocalDate> absentWorkingDays = toAbsentWorkingDays(absencePeriod);

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
                .absentWorkingDays(absentWorkingDays)
                .build();

        };
    }

    private static Function<AbsencePeriod, SickNoteUpdatedEventDTO> toSickNoteUpdatedEventDTO(String tenantId, SickNoteUpdatedEvent event) {
        return absencePeriod -> {
            final SickNotePersonDTO person = toSickNotePersonDTO(event.sickNote().getPerson());
            final SickNotePersonDTO applier = event.sickNote().getApplier() != null ? toSickNotePersonDTO(event.sickNote().getApplier()) : null;
            final SickNotePeriodDTO period = toPeriod(event.sickNote());
            final SickNotePeriodDTO medicalCertificatePeriod = toMedicalCertificatePeriod(event.sickNote());
            final Set<LocalDate> absentWorkingDays = toAbsentWorkingDays(absencePeriod);

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
                .absentWorkingDays(absentWorkingDays)
                .build();

        };
    }
    private static Function<AbsencePeriod, SickNoteAcceptedEventDTO> toSickNoteAcceptedEventDTO(String tenantId, SickNoteAcceptedEvent event) {
        return absencePeriod -> {
            final SickNotePersonDTO person = toSickNotePersonDTO(event.sickNote().getPerson());
            final SickNotePersonDTO applier = event.sickNote().getApplier() != null ? toSickNotePersonDTO(event.sickNote().getApplier()) : null;
            final SickNotePeriodDTO period = toPeriod(event.sickNote());
            final SickNotePeriodDTO medicalCertificatePeriod = toMedicalCertificatePeriod(event.sickNote());
            final Set<LocalDate> absentWorkingDays = toAbsentWorkingDays(absencePeriod);

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
                .absentWorkingDays(absentWorkingDays)
                .build();

        };
    }

    private static Function<AbsencePeriod, SickNoteConvertedToApplicationEventDTO> toSickNoteConvertedEventDTO(String tenantId, SickNoteToApplicationConvertedEvent event) {
        return absencePeriod -> {
            final SickNotePersonDTO person = toSickNotePersonDTO(event.sickNote().getPerson());
            final SickNotePersonDTO applier = event.sickNote().getApplier() != null ? toSickNotePersonDTO(event.sickNote().getApplier()) : null;
            final SickNotePeriodDTO period = toPeriod(event.sickNote());
            final SickNotePeriodDTO medicalCertificatePeriod = toMedicalCertificatePeriod(event.sickNote());
            final Set<LocalDate> absentWorkingDays = toAbsentWorkingDays(absencePeriod);

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
                .absentWorkingDays(absentWorkingDays)
                .build();
        };
    }

    @EventListener
    @Async
    void on(SickNoteCancelledEvent event) {
        getClosedAbsencePeriods(event.sickNote())
            .map(toSickNoteCancelledEventDTO(tenantSupplier.get(), event))
            .ifPresent(applicationEventPublisher::publishEvent);
    }

    @EventListener
    @Async
    void on(SickNoteCreatedEvent event) {
        getAbsencePeriods(event.sickNote())
            .map(toSickNoteCreatedEventDTO(tenantSupplier.get(), event))
            .ifPresent(applicationEventPublisher::publishEvent);
    }

    @EventListener
    @Async
    void on(SickNoteUpdatedEvent event) {
        getAbsencePeriods(event.sickNote())
            .map(toSickNoteUpdatedEventDTO(tenantSupplier.get(), event))
            .ifPresent(applicationEventPublisher::publishEvent);
    }

    @EventListener
    @Async
    void on(SickNoteAcceptedEvent event) {
        getAbsencePeriods(event.sickNote())
            .map(toSickNoteAcceptedEventDTO(tenantSupplier.get(), event))
            .ifPresent(applicationEventPublisher::publishEvent);
    }

    @EventListener
    @Async
    void on(SickNoteToApplicationConvertedEvent event) {
        getAbsencePeriods(event.sickNote())
            .map(toSickNoteConvertedEventDTO(tenantSupplier.get(), event))
            .ifPresent(applicationEventPublisher::publishEvent);
    }

    private Optional<AbsencePeriod> getAbsencePeriods(SickNote sickNote) {
        return absenceService.getOpenAbsences(sickNote.getPerson(), sickNote.getStartDate(), sickNote.getEndDate()).stream()
            .filter(isFullOrSameDayLength(sickNote.getDayLength()))
            .findFirst();
    }

    private Optional<AbsencePeriod> getClosedAbsencePeriods(SickNote sickNote) {
        return absenceService.getClosedAbsences(sickNote.getPerson(), sickNote.getStartDate(), sickNote.getEndDate()).stream()
            .filter(isFullOrSameDayLength(sickNote.getDayLength()))
            .findFirst();
    }

    private static Predicate<AbsencePeriod> isFullOrSameDayLength(org.synyx.urlaubsverwaltung.period.DayLength dayLength) {
        return isFullDay(dayLength).or(isMorning(dayLength)).or(isNoon(dayLength));
    }

    private static Predicate<AbsencePeriod> isFullDay(org.synyx.urlaubsverwaltung.period.DayLength dayLength) {
        return absencePeriod -> dayLength.isFull();
    }

    private static Predicate<AbsencePeriod> isMorning(org.synyx.urlaubsverwaltung.period.DayLength dayLength) {
        return absencePeriod -> dayLength.isMorning() && absencePeriod.absenceRecords().stream().allMatch(absenceRecord -> absenceRecord.getMorning().isPresent());
    }

    private static Predicate<AbsencePeriod> isNoon(org.synyx.urlaubsverwaltung.period.DayLength dayLength) {
        return absencePeriod -> dayLength.isNoon() && absencePeriod.absenceRecords().stream().allMatch(absenceRecord -> absenceRecord.getNoon().isPresent());
    }
}
