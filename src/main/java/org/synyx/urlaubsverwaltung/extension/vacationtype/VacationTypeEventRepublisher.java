package org.synyx.urlaubsverwaltung.extension.vacationtype;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdatedEvent;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnProperty(value = "uv.extensions.vacationtype.republish.enabled", havingValue = "true")
@ConditionalOnBean(VacationTypeEventHandlerExtension.class)
public class VacationTypeEventRepublisher {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final VacationTypeService vacationTypeService;
    private final VacationTypeEventHandlerExtension vacationTypeEventHandlerExtension;

    VacationTypeEventRepublisher(VacationTypeService vacationTypeService,
                                 VacationTypeEventHandlerExtension vacationTypeEventHandlerExtension) {
        this.vacationTypeService = vacationTypeService;
        this.vacationTypeEventHandlerExtension = vacationTypeEventHandlerExtension;
    }

    public void republishEvents() {
        LOG.info("Republishing all events with type=VacationTypeUpdatedEvent");
        vacationTypeService.getAllVacationTypes()
            .stream()
            .map(VacationTypeUpdatedEvent::of)
            .forEach(event -> {
                final VacationType<?> updatedVacationType = event.updatedVacationType();
                LOG.info("Publishing vacationTypeUpdatedEvent with id={}, vacationTypeId={} for category={} with active={} and requiresApprovalToApply={}",
                    event.id(), updatedVacationType.getId(), updatedVacationType.getCategory(), updatedVacationType.isActive(), updatedVacationType.isRequiresApprovalToApply());
                vacationTypeEventHandlerExtension.onVacationTypeUpdated(event);
            });
        LOG.info("Republished all events with type=VacationTypeUpdatedEvent");
    }
}
