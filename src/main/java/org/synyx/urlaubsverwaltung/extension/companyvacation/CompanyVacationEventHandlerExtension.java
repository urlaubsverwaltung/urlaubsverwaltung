package org.synyx.urlaubsverwaltung.extension.companyvacation;

import de.focus_shift.urlaubsverwaltung.extension.api.companyvacation.CompanyVacationDeletedEventDto;
import de.focus_shift.urlaubsverwaltung.extension.api.companyvacation.CompanyVacationPeriodDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.companyvacation.CompanyVacationPublishedEventDto;
import de.focus_shift.urlaubsverwaltung.extension.api.companyvacation.DayLength;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.companyvacation.CompanyVacationDeletedEvent;
import org.synyx.urlaubsverwaltung.companyvacation.CompanyVacationPublishedEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnProperty(value = "uv.extensions.enabled", havingValue = "true")
class CompanyVacationEventHandlerExtension {

    private static final Logger LOG = getLogger(lookup().lookupClass());
    private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.systemDefault();

    private final TenantSupplier tenantSupplier;
    private final ApplicationEventPublisher applicationEventPublisher;

    CompanyVacationEventHandlerExtension(TenantSupplier tenantSupplier, ApplicationEventPublisher applicationEventPublisher) {
        this.tenantSupplier = tenantSupplier;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void on(CompanyVacationPublishedEvent event) {
        final CompanyVacationPublishedEventDto companyVacationPublishedEventDto = toCompanyVacationPublishedEventDto(tenantSupplier.get(), event);
        applicationEventPublisher.publishEvent(companyVacationPublishedEventDto);
        LOG.info("Published CompanyVacationPublishedEventDto for {}", companyVacationPublishedEventDto);
    }

    @EventListener
    void on(CompanyVacationDeletedEvent event) {
        final CompanyVacationDeletedEventDto companyVacationDeletedEventDto = toCompanyVacationDeletedEventDto(tenantSupplier.get(), event);
        applicationEventPublisher.publishEvent(companyVacationDeletedEventDto);
        LOG.info("Published CompanyVacationDeletedEventDto for {}", companyVacationDeletedEventDto);
    }

    private CompanyVacationPublishedEventDto toCompanyVacationPublishedEventDto(String tenantId, CompanyVacationPublishedEvent event) {
        return CompanyVacationPublishedEventDto.builder()
            .id(event.id())
            .sourceId(event.sourceId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .period(toPeriod(event))
            .build();
    }

    private static CompanyVacationPeriodDTO toPeriod(CompanyVacationPublishedEvent event) {
        return CompanyVacationPeriodDTO.builder()
            .startDate(localDateToInstant(event.startDate()))
            .endDate(localDateToInstant(event.endDate()))
            .dayLength(DayLength.valueOf(event.dayLength().name()))
            .build();
    }

    private static Instant localDateToInstant(LocalDate localDate) {
        return localDate.atStartOfDay(DEFAULT_TIME_ZONE).toInstant();
    }

    private CompanyVacationDeletedEventDto toCompanyVacationDeletedEventDto(String tenantId, CompanyVacationDeletedEvent event) {
        return CompanyVacationDeletedEventDto.builder()
            .id(event.id())
            .sourceId(event.sourceId())
            .createdAt(event.createdAt())
            .tenantId(tenantId)
            .build();
    }
}
