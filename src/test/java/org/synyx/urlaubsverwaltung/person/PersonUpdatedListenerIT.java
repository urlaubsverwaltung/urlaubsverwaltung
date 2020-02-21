package org.synyx.urlaubsverwaltung.person;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { PersonUpdatedListener.class, PersonUpdatedListenerIT.PersonDisabledListenerDummy.class })
public class PersonUpdatedListenerIT {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private PersonUpdatedListenerIT.PersonDisabledListenerDummy personDisabledListenerDummy;

    private ArgumentCaptor<PersonDisabledEvent> personDisabledEventArgumentCaptor = ArgumentCaptor.forClass(PersonDisabledEvent.class);

    @Test
    public void ensurePersonDisabledEventIsNotFiredWhenPersonRoleToggledToInactive() {

        final Person personBeforeUpdate = createPerson();
        personBeforeUpdate.setId(1);
        personBeforeUpdate.setPermissions(List.of(Role.USER));

        final Person personAfterUpdate = createPerson();
        personAfterUpdate.setId(1);
        personAfterUpdate.setPermissions(List.of(Role.USER));

        applicationEventPublisher.publishEvent(new PersonUpdatedEvent(this, personBeforeUpdate, personAfterUpdate));

        verifyZeroInteractions(personDisabledListenerDummy);
    }

    @Test
    public void ensurePersonDisabledEventIsFiredWhenPersonRoleToggledToInactive() {

        final Person personBeforeUpdate = createPerson();
        personBeforeUpdate.setId(1);
        personBeforeUpdate.setPermissions(List.of(Role.USER));

        final Person personAfterUpdate = createPerson();
        personAfterUpdate.setId(1);
        personAfterUpdate.setPermissions(List.of(Role.INACTIVE));

        applicationEventPublisher.publishEvent(new PersonUpdatedEvent(this, personBeforeUpdate, personAfterUpdate));

        verify(personDisabledListenerDummy).onPersonDisabled(personDisabledEventArgumentCaptor.capture());

        final PersonDisabledEvent actualPersonDisabledEvent = personDisabledEventArgumentCaptor.getValue();
        assertThat(actualPersonDisabledEvent.getPersonId()).isEqualTo(personAfterUpdate.getId());
    }

    @Test
    public void ensurePersonDisabledEventIsNotFiredWhenPersonRoleHasBeenInactiveAlready() {

        final Person personBeforeUpdate = createPerson();
        personBeforeUpdate.setId(1);
        personBeforeUpdate.setPermissions(List.of(Role.INACTIVE));

        final Person personAfterUpdate = createPerson();
        personAfterUpdate.setId(1);
        personAfterUpdate.setPermissions(List.of(Role.INACTIVE));

        applicationEventPublisher.publishEvent(new PersonUpdatedEvent(this, personBeforeUpdate, personAfterUpdate));

        verifyZeroInteractions(personDisabledListenerDummy);
    }

    static class PersonDisabledListenerDummy {
        @EventListener
        public void onPersonDisabled(PersonDisabledEvent event) {
            // do awesome stuff with the event
        }
    }
}
