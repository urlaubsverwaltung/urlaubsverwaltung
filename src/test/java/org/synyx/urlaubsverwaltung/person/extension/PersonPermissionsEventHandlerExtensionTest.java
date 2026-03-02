package org.synyx.urlaubsverwaltung.person.extension;

import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonPermissionsChangedEventDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.tenancy.TenantSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonPermissionsChangedEvent;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonPermissionsEventHandlerExtensionTest {

    @Mock
    private TenantSupplier tenantSupplier;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PersonPermissionsEventHandlerExtension sut;

    @Captor
    private ArgumentCaptor<PersonPermissionsChangedEventDTO> argumentCaptor;

    @Test
    void ensurePersonPermissionsChangedEventIsConvertedToDTO() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(Set.of(USER));

        final PersonPermissionsChangedEvent event = PersonPermissionsChangedEvent.of(
            person, List.of(USER, BOSS), List.of(USER, OFFICE)
        );

        when(tenantSupplier.get()).thenReturn("default");

        sut.on(event);

        verify(tenantSupplier).get();
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

        final PersonPermissionsChangedEventDTO result = argumentCaptor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(result.tenantId()).isEqualTo("default");
        assertThat(result.username()).isEqualTo("muster");
        assertThat(result.currentPermissions()).containsExactlyInAnyOrder("USER", "OFFICE");
        assertThat(result.grantedPermissions()).containsExactly("OFFICE");
        assertThat(result.revokedPermissions()).containsExactly("BOSS");
    }

    @Test
    void ensureInactiveRoleIsFilteredOut() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(Set.of(USER));

        final PersonPermissionsChangedEvent event = PersonPermissionsChangedEvent.of(
            person, List.of(USER, INACTIVE), List.of(USER, OFFICE, INACTIVE)
        );

        when(tenantSupplier.get()).thenReturn("default");

        sut.on(event);

        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

        final PersonPermissionsChangedEventDTO result = argumentCaptor.getValue();

        assertThat(result.currentPermissions()).containsExactlyInAnyOrder("USER", "OFFICE");
        assertThat(result.grantedPermissions()).containsExactly("OFFICE");
        assertThat(result.revokedPermissions()).isEmpty();
    }
}
