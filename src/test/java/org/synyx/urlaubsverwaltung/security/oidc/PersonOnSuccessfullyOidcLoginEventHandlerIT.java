package org.synyx.urlaubsverwaltung.security.oidc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.SUB;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.FAMILY_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.GIVEN_NAME;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@SpringBootTest
class PersonOnSuccessfullyOidcLoginEventHandlerIT extends SingleTenantTestContainersBase {

    private static final String UNIQUE_ID = "uniqueID";
    private static final String EMAIL_ADDRESS = "muster@example.org";

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private PersonService personService;
    @Autowired
    private OidcPersonAuthoritiesMapper oidcPersonAuthoritiesMapper;

    @BeforeEach
    @AfterEach
    void deleteAllPersons() {
        personService.getAllPersons()
            .forEach(person -> personService.delete(person.getIdAsPersonId(), person.getIdAsPersonId()));
    }

    @Test
    void ensureFirstPersonLoggingInBecomesOfficeUser() {

        // fresh installation, no person exists at all
        final Collection<? extends GrantedAuthority> sessionAuthorities = login();

        assertThat(signedInPerson().getPermissions()).contains(OFFICE);
        assertThat(sessionAuthorities).extracting(GrantedAuthority::getAuthority).contains(OFFICE.name());
    }

    @Test
    void ensureFirstPersonLoggingInBecomesOfficeUserAlthoughOtherPersonsExistWithoutOfficeRole() {

        // colleagues were provisioned beforehand, none of them holds the office role
        personService.create("colleague@example.org", "Klaus", "Kollege", "colleague@example.org");

        final Collection<? extends GrantedAuthority> sessionAuthorities = login();

        assertThat(signedInPerson().getPermissions()).contains(OFFICE);
        assertThat(sessionAuthorities).extracting(GrantedAuthority::getAuthority).contains(OFFICE.name());
    }

    @Test
    void ensureFirstPersonLoggingInBecomesOfficeUserAlthoughPersonWasProvisionedBeforehand() {

        // the person signing in was provisioned before anybody logged in, e.g. via the person api.
        // the username is the mail address there, the oidc subject is not known yet.
        personService.create(EMAIL_ADDRESS, "Marlene", "Muster", EMAIL_ADDRESS);

        final Collection<? extends GrantedAuthority> sessionAuthorities = login();

        assertThat(signedInPerson().getPermissions()).contains(OFFICE);
        assertThat(sessionAuthorities).extracting(GrantedAuthority::getAuthority).contains(OFFICE.name());
    }

    @Test
    void ensureFirstPersonLoggingInBecomesOfficeUserAlthoughPersonWasProvisionedBeforehandWithOidcSubjectAsUsername() {

        // same, but the person is resolved by username instead of by the mail address fallback
        personService.create(UNIQUE_ID, "Marlene", "Muster", EMAIL_ADDRESS);

        final Collection<? extends GrantedAuthority> sessionAuthorities = login();

        assertThat(signedInPerson().getPermissions()).contains(OFFICE);
        assertThat(sessionAuthorities).extracting(GrantedAuthority::getAuthority).contains(OFFICE.name());
    }

    private Collection<? extends GrantedAuthority> login() {

        // nobody holds the office role when signing in
        assertThat(personService.getActivePersonsByRole(OFFICE)).isEmpty();

        final OidcIdToken idToken = new OidcIdToken("tokenValue", Instant.now(), Instant.MAX, Map.of(
            SUB, UNIQUE_ID,
            GIVEN_NAME, "Marlene",
            FAMILY_NAME, "Muster",
            EMAIL, EMAIL_ADDRESS
        ));

        // authorities are mapped while authenticating, before the AuthenticationSuccessEvent is published
        final Collection<? extends GrantedAuthority> sessionAuthorities =
            oidcPersonAuthoritiesMapper.mapAuthorities(List.of(new OidcUserAuthority(idToken)));

        final OAuth2LoginAuthenticationToken authentication = mock(OAuth2LoginAuthenticationToken.class);
        when(authentication.getPrincipal()).thenReturn(new DefaultOidcUser(List.of(new SimpleGrantedAuthority("USER")), idToken));
        applicationEventPublisher.publishEvent(new AuthenticationSuccessEvent(authentication));

        return sessionAuthorities;
    }

    private Person signedInPerson() {
        return personService.getPersonByUsername(UNIQUE_ID).orElseThrow();
    }
}
