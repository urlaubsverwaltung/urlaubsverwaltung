package org.synyx.urlaubsverwaltung.person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;

@Component
public class PersonUpdatedListener {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public PersonUpdatedListener(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    public void onPersonUpdatedEvent(PersonUpdatedEvent event) {

        final Person personBeforeUpdate = event.getPersonBeforeUpdate();
        final Person personAfterUpdate = event.getPersonAfterUpdate();

        final boolean hasBeenInactive = personBeforeUpdate.getPermissions().contains(INACTIVE);
        if (!hasBeenInactive) {
            final boolean isNowInactive = personAfterUpdate.getPermissions().contains(INACTIVE);
            if (isNowInactive) {
                applicationEventPublisher.publishEvent(new PersonDisabledEvent(this, personAfterUpdate.getId()));
            }
        }
    }
}
