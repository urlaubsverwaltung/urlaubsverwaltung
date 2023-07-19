package org.synyx.urlaubsverwaltung.extension.application;

import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationAllowedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationCancelledEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationCreatedFromSickNoteEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationPeriodDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationPersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.DayLength;
import de.focus_shift.urlaubsverwaltung.extension.api.application.VacationTypeDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.absence.AbsencePeriod;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAllowedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationCancelledEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationCreatedFromSickNoteEvent;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.person.Person;

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
public class ApplicationEventHandlerExtension {

    private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.systemDefault();

    private final TenantSupplier tenantSupplier;
    private final AbsenceService absenceService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ApplicationEventHandlerExtension(TenantSupplier tenantSupplier,
                                            AbsenceService absenceService,
                                            ApplicationEventPublisher applicationEventPublisher) {
        this.tenantSupplier = tenantSupplier;
        this.absenceService = absenceService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private static Set<LocalDate> toAbsentWorkingDays(AbsencePeriod absencePeriod) {
        return absencePeriod.getAbsenceRecords()
            .stream()
            .map(AbsencePeriod.Record::getDate)
            .collect(Collectors.toSet());
    }

    private static ApplicationPeriodDTO toPeriod(Application application) {
        return ApplicationPeriodDTO.builder()
            .startDate(localDateToInstant(application.getStartDate()))
            .endDate(localDateToInstant(application.getEndDate()))
            .dayLength(DayLength.valueOf(application.getDayLength().name()))
            .build();
    }

    private static ApplicationPersonDTO toApplicationPersonDTO(Person person) {
        return ApplicationPersonDTO.builder()
            .personId(person.getId())
            .username(person.getUsername())
            .build();
    }

    private static Instant localDateToInstant(LocalDate localDate) {
        return localDate.atStartOfDay(DEFAULT_TIME_ZONE).toInstant();
    }

    private static String toStatus(Application application) {
        return application.getStatus().name();
    }

    private static Function<AbsencePeriod, ApplicationCreatedFromSickNoteEventDTO> toApplicationCreatedFromSickNoteEventDTO(String tenantId, ApplicationCreatedFromSickNoteEvent event) {
        return absencePeriod -> {
            final ApplicationPersonDTO person = toApplicationPersonDTO(event.getApplication().getPerson());
            final ApplicationPersonDTO appliedBy = event.getApplication().getApplier() != null ? toApplicationPersonDTO(event.getApplication().getApplier()) : null;
            final ApplicationPeriodDTO period = toPeriod(event.getApplication());
            final VacationTypeDTO vacationType = toVacationType(event.getApplication().getVacationType());
            final String status = toStatus(event.getApplication());
            final Set<LocalDate> absentWorkingDays = toAbsentWorkingDays(absencePeriod);

            return ApplicationCreatedFromSickNoteEventDTO.builder()
                .id(event.getId())
                .sourceId(event.getApplication().getId())
                .createdAt(event.getCreatedAt())
                .tenantId(tenantId)
                .person(person)
                .appliedBy(appliedBy)
                .period(period)
                .vacationType(vacationType)
                .reason(event.getApplication().getReason())
                .status(status)
                .teamInformed(event.getApplication().isTeamInformed())
                .absentWorkingDays(absentWorkingDays)
                .build();
        };
    }

    private static Function<AbsencePeriod, ApplicationAllowedEventDTO> toApplicationAllowedEventDTO(String tenantId, ApplicationAllowedEvent event) {
        return absencePeriod -> {
            final ApplicationPersonDTO person = toApplicationPersonDTO(event.getApplication().getPerson());
            final ApplicationPersonDTO appliedBy = event.getApplication().getApplier() != null ? toApplicationPersonDTO(event.getApplication().getApplier()) : null;
            final ApplicationPersonDTO allowedBy = event.getApplication().getBoss() != null ? toApplicationPersonDTO(event.getApplication().getBoss()) : null;
            final ApplicationPeriodDTO period = toPeriod(event.getApplication());
            final VacationTypeDTO vacationType = toVacationType(event.getApplication().getVacationType());
            final String status = toStatus(event.getApplication());
            final Set<LocalDate> absentWorkingDays = toAbsentWorkingDays(absencePeriod);

            return ApplicationAllowedEventDTO.builder()
                .id(event.getId())
                .sourceId(event.getApplication().getId())
                .createdAt(event.getCreatedAt())
                .tenantId(tenantId)
                .person(person)
                .appliedBy(appliedBy)
                .allowedBy(allowedBy)
                .twoStageApproval(event.getApplication().isTwoStageApproval())
                .period(period)
                .vacationType(vacationType)
                .reason(event.getApplication().getReason())
                .status(status)
                .teamInformed(event.getApplication().isTeamInformed())
                .absentWorkingDays(absentWorkingDays)
                .build();
        };
    }

    private static Function<AbsencePeriod, ApplicationCancelledEventDTO> toApplicationCancelledEventDTO(String tenantId, ApplicationCancelledEvent event) {
        return absencePeriod -> {
            final ApplicationPersonDTO person = toApplicationPersonDTO(event.getApplication().getPerson());
            final ApplicationPersonDTO appliedBy = event.getApplication().getApplier() != null ? toApplicationPersonDTO(event.getApplication().getApplier()) : null;
            final ApplicationPersonDTO cancelledBy = event.getApplication().getCanceller() != null ? toApplicationPersonDTO(event.getApplication().getCanceller()) : null;
            final ApplicationPeriodDTO period = toPeriod(event.getApplication());
            final VacationTypeDTO vacationType = toVacationType(event.getApplication().getVacationType());
            final String status = toStatus(event.getApplication());
            final Set<LocalDate> absentWorkingDays = toAbsentWorkingDays(absencePeriod);

            return ApplicationCancelledEventDTO.builder()
                .id(event.getId())
                .sourceId(event.getApplication().getId())
                .createdAt(event.getCreatedAt())
                .tenantId(tenantId)
                .person(person)
                .appliedBy(appliedBy)
                .cancelledBy(cancelledBy)
                .twoStageApproval(event.getApplication().isTwoStageApproval())
                .period(period)
                .vacationType(vacationType)
                .reason(event.getApplication().getReason())
                .status(status)
                .teamInformed(event.getApplication().isTeamInformed())
                .absentWorkingDays(absentWorkingDays)
                .build();
        };
    }

    private static VacationTypeDTO toVacationType(VacationTypeEntity vacationType) {
        return VacationTypeDTO.builder()
            .category(vacationType.getCategory().name())
            .requiresApprovalToApply(vacationType.isRequiresApprovalToApply())
            .requiresApprovalToCancel(vacationType.isRequiresApprovalToCancel())
            .color(vacationType.getColor().name())
            .visibleToEveryone(vacationType.isVisibleToEveryone())
            .build();
    }

    @EventListener
    @Async
    void on(ApplicationAllowedEvent event) {
        getAbsencePeriods(event.getApplication())
            .map(toApplicationAllowedEventDTO(tenantSupplier.get(), event))
            .ifPresent(applicationEventPublisher::publishEvent);
    }

    @EventListener
    @Async
    void on(ApplicationCancelledEvent event) {
        getClosedAbsencePeriods(event.getApplication())
            .map(toApplicationCancelledEventDTO(tenantSupplier.get(), event))
            .ifPresent(applicationEventPublisher::publishEvent);
    }

    @EventListener
    @Async
    void on(ApplicationCreatedFromSickNoteEvent event) {
        getAbsencePeriods(event.getApplication())
            .map(toApplicationCreatedFromSickNoteEventDTO(tenantSupplier.get(), event))
            .ifPresent(applicationEventPublisher::publishEvent);
    }

    private Optional<AbsencePeriod> getAbsencePeriods(Application application) {
        return absenceService.getOpenAbsences(application.getPerson(), application.getStartDate(), application.getEndDate()).stream()
            .filter(isFullOrSameDayLength(application.getDayLength()))
            .findFirst();
    }

    private Optional<AbsencePeriod> getClosedAbsencePeriods(Application application) {
        return absenceService.getClosedAbsences(application.getPerson(), application.getStartDate(), application.getEndDate()).stream()
            .filter(isFullOrSameDayLength(application.getDayLength()))
            .findFirst();
    }

    private static Predicate<AbsencePeriod> isFullOrSameDayLength(org.synyx.urlaubsverwaltung.period.DayLength dayLength) {
        return isFullDay(dayLength).or(isMorning(dayLength)).or(isNoon(dayLength));
    }

    private static Predicate<AbsencePeriod> isFullDay(org.synyx.urlaubsverwaltung.period.DayLength dayLength) {
        return absencePeriod -> dayLength.isFull();
    }

    private static Predicate<AbsencePeriod> isMorning(org.synyx.urlaubsverwaltung.period.DayLength dayLength) {
        return absencePeriod -> dayLength.isMorning() && absencePeriod.getAbsenceRecords().stream().allMatch(absenceRecord -> absenceRecord.getMorning().isPresent());
    }

    private static Predicate<AbsencePeriod> isNoon(org.synyx.urlaubsverwaltung.period.DayLength dayLength) {
        return absencePeriod -> dayLength.isNoon() && absencePeriod.getAbsenceRecords().stream().allMatch(absenceRecord -> absenceRecord.getNoon().isPresent());
    }
}
