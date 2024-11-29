package org.synyx.urlaubsverwaltung.sicknote.sicknotetype;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Component
@ConditionalOnSingleTenantMode
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
