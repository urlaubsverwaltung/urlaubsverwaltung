package org.synyx.urlaubsverwaltung.sicknote.sicknotetype;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.IsSingleTenantMode;

import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationStartedEvent;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@Conditional(IsSingleTenantMode.class)
class InitialDefaultSickNoteTypesConfigurerSingleTenant {

    private final SickNoteTypeService sickNoteTypeService;

    InitialDefaultSickNoteTypesConfigurerSingleTenant(SickNoteTypeService sickNoteTypeService) {
        this.sickNoteTypeService = sickNoteTypeService;
    }

    @EventListener(ApplicationStartedEvent.class)
    void insertDefaultSickNoteTypes() {
        sickNoteTypeService.insertDefaultSickNoteTypes();
    }
}
