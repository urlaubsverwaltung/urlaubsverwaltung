package org.synyx.urlaubsverwaltung.extension.application;

import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationAllowedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationCancelledEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationCreatedFromSickNoteEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationPeriodDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationPersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.ApplicationUpdatedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.application.DayLength;
import de.focus_shift.urlaubsverwaltung.extension.api.application.VacationTypeDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAllowedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationCancelledEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationCreatedFromSickNoteEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationUpdatedEvent;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@ConditionalOnProperty(value = "uv.extensions.enabled", havingValue = "true")
public class ApplicationEventHandlerExtension {

    private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.systemDefault();

    private final TenantSupplier tenantSupplier;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ApplicationEventHandlerExtension(
        TenantSupplier tenantSupplier,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.tenantSupplier = tenantSupplier;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void on(ApplicationAllowedEvent event) {
        applicationEventPublisher.publishEvent(toApplicationAllowedEventDTO(tenantSupplier.get(), event));

    }

    @EventListener
    void on(ApplicationUpdatedEvent event) {
        applicationEventPublisher.publishEvent(toApplicationUpdatedEventDTO(tenantSupplier.get(), event));
    }

    @EventListener
    void on(ApplicationCancelledEvent event) {
        applicationEventPublisher.publishEvent(toApplicationCancelledEventDTO(tenantSupplier.get(), event));
    }

    @EventListener
    void on(ApplicationCreatedFromSickNoteEvent event) {
        applicationEventPublisher.publishEvent(toApplicationCreatedFromSickNoteEventDTO(tenantSupplier.get(), event));
    }

    private static String getTranslationKey(VacationType<?> vacationType) {
        if (vacationType instanceof ProvidedVacationType providedVacationType) {
            return providedVacationType.getMessageKey();
        }
        return null;
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

    private static ApplicationCreatedFromSickNoteEventDTO toApplicationCreatedFromSickNoteEventDTO(String tenantId, ApplicationCreatedFromSickNoteEvent event) {
        final VacationTypeDTO vacationType = toVacationType(event.application().getVacationType());
        final ApplicationPersonDTO person = toApplicationPersonDTO(event.application().getPerson());
        final ApplicationPersonDTO appliedBy = event.application().getApplier() != null ? toApplicationPersonDTO(event.application().getApplier()) : null;
        final ApplicationPeriodDTO period = toPeriod(event.application());
        final String status = toStatus(event.application());

        return ApplicationCreatedFromSickNoteEventDTO.builder()
            .id(event.id())
            .sourceId(event.application().getId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .person(person)
            .appliedBy(appliedBy)
            .period(period)
            .vacationType(vacationType)
            .reason(event.application().getReason())
            .status(status)
            .teamInformed(event.application().isTeamInformed())
            .build();
    }

    private static ApplicationAllowedEventDTO toApplicationAllowedEventDTO(String tenantId, ApplicationAllowedEvent event) {
        final VacationTypeDTO vacationType = toVacationType(event.application().getVacationType());
        final ApplicationPersonDTO person = toApplicationPersonDTO(event.application().getPerson());
        final ApplicationPersonDTO appliedBy = event.application().getApplier() != null ? toApplicationPersonDTO(event.application().getApplier()) : null;
        final ApplicationPersonDTO allowedBy = event.application().getBoss() != null ? toApplicationPersonDTO(event.application().getBoss()) : null;
        final ApplicationPeriodDTO period = toPeriod(event.application());
        final String status = toStatus(event.application());

        return ApplicationAllowedEventDTO.builder()
            .id(event.id())
            .sourceId(event.application().getId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .person(person)
            .appliedBy(appliedBy)
            .allowedBy(allowedBy)
            .twoStageApproval(event.application().isTwoStageApproval())
            .period(period)
            .vacationType(vacationType)
            .reason(event.application().getReason())
            .status(status)
            .teamInformed(event.application().isTeamInformed())
            .hours(event.application().getHours())
            .build();
    }

    private static ApplicationUpdatedEventDTO toApplicationUpdatedEventDTO(String tenantId, ApplicationUpdatedEvent event) {
        final VacationTypeDTO vacationType = toVacationType(event.application().getVacationType());
        final ApplicationPersonDTO person = toApplicationPersonDTO(event.application().getPerson());
        final ApplicationPersonDTO appliedBy = event.application().getApplier() != null ? toApplicationPersonDTO(event.application().getApplier()) : null;
        final ApplicationPersonDTO allowedBy = event.application().getBoss() != null ? toApplicationPersonDTO(event.application().getBoss()) : null;
        final ApplicationPeriodDTO period = toPeriod(event.application());
        final String status = toStatus(event.application());

        return ApplicationUpdatedEventDTO.builder()
            .id(event.id())
            .sourceId(event.application().getId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .person(person)
            .appliedBy(appliedBy)
            .updatedBy(allowedBy)
            .twoStageApproval(event.application().isTwoStageApproval())
            .period(period)
            .vacationType(vacationType)
            .reason(event.application().getReason())
            .status(status)
            .teamInformed(event.application().isTeamInformed())
            .hours(event.application().getHours())
            .build();
    }


    private static ApplicationCancelledEventDTO toApplicationCancelledEventDTO(String tenantId, ApplicationCancelledEvent event) {
        final VacationTypeDTO vacationType = toVacationType(event.application().getVacationType());
        final ApplicationPersonDTO person = toApplicationPersonDTO(event.application().getPerson());
        final ApplicationPersonDTO appliedBy = event.application().getApplier() != null ? toApplicationPersonDTO(event.application().getApplier()) : null;
        final ApplicationPersonDTO cancelledBy = event.application().getCanceller() != null ? toApplicationPersonDTO(event.application().getCanceller()) : null;
        final ApplicationPeriodDTO period = toPeriod(event.application());
        final String status = toStatus(event.application());

        return ApplicationCancelledEventDTO.builder()
            .id(event.id())
            .sourceId(event.application().getId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .person(person)
            .appliedBy(appliedBy)
            .cancelledBy(cancelledBy)
            .twoStageApproval(event.application().isTwoStageApproval())
            .period(period)
            .vacationType(vacationType)
            .reason(event.application().getReason())
            .status(status)
            .teamInformed(event.application().isTeamInformed())
            .hours(event.application().getHours())
            .build();
    }

    private static VacationTypeDTO toVacationType(VacationType<?> vacationType) {
        return VacationTypeDTO.builder()
            .sourceId(vacationType.getId())
            .category(vacationType.getCategory().name())
            .requiresApprovalToApply(vacationType.isRequiresApprovalToApply())
            .requiresApprovalToCancel(vacationType.isRequiresApprovalToCancel())
            .color(vacationType.getColor().name())
            .visibleToEveryone(vacationType.isVisibleToEveryone())
            .translationKey(getTranslationKey(vacationType))
            .build();
    }
}
