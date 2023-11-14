package org.synyx.urlaubsverwaltung.extension.vacationtype;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdatedEvent;

import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnProperty(value = "uv.extensions.vacationtype.republish.enabled", havingValue = "true")
@ConditionalOnBean(VacationTypeEventHandlerExtension.class)
class VacationTypeEventRepublisher {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final VacationTypeService vacationTypeService;
    private final VacationTypeEventHandlerExtension vacationTypeEventHandlerExtension;

    VacationTypeEventRepublisher(VacationTypeService vacationTypeService,
                                 VacationTypeEventHandlerExtension vacationTypeEventHandlerExtension) {
        this.vacationTypeService = vacationTypeService;
        this.vacationTypeEventHandlerExtension = vacationTypeEventHandlerExtension;
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    void republishEvents() {

        LOG.info("Republishing all events with type=VacationTypesUpdatedEvent");

        final List<VacationType<?>> allVacationTypes = vacationTypeService.getAllVacationTypes();

        for (VacationType<?> vacationType : allVacationTypes) {
            final VacationTypeUpdatedEvent event = new VacationTypeUpdatedEvent(vacationType);
            LOG.info("Publishing VacationTypeUpdatedEvent with vacationType id={}", event.updatedVacationType().getId());
            vacationTypeEventHandlerExtension.on(event);
        }

        LOG.info("Republished all events with type=VacationTypesUpdatedEvent");
    }
}
