package org.synyx.urlaubsverwaltung.security.oidc;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.SUB;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.FAMILY_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.GIVEN_NAME;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonOnSuccessfullyOidcLoginEventHandlerTest {

    private PersonOnSuccessfullyOidcLoginEventHandler sut;

    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new PersonOnSuccessfullyOidcLoginEventHandler(personService);
    }

    @Nested
    class CreatePerson {

        @Test
        void createNewPerson() {
            final String uniqueID = "uniqueID";
            final String givenName = "given name";
            final String familyName = "family name";
            final String email = "test.me@example.com";

            final AuthenticationSuccessEvent event = getOidcUserAuthority(
                List.of(new SimpleGrantedAuthority("USER")),
                Map.of(
                    SUB, uniqueID,
                    GIVEN_NAME, givenName,
                    FAMILY_NAME, familyName,
                    EMAIL, email
                ));

            when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());
            when(personService.getPersonByMailAddress(email)).thenReturn(Optional.empty());
            when(personService.create(uniqueID, givenName, familyName, email)).thenReturn(new Person(uniqueID, familyName, givenName, email));

            sut.handle(event);

            verify(personService).getPersonByUsername(uniqueID);
            verify(personService).getPersonByMailAddress(email);

            verify(personService).create(uniqueID, givenName, familyName, email);

            final ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
            verify(personService).appointAsOfficeUserIfNoOfficeUserPresent(personArgumentCaptor.capture());

            Person created = personArgumentCaptor.getValue();
            assertThat(created.getUsername()).isEqualTo(uniqueID);
            assertThat(created.getLastName()).isEqualTo(familyName);
            assertThat(created.getFirstName()).isEqualTo(givenName);
            assertThat(created.getEmail()).isEqualTo(email);
        }
    }

    @Nested
    class UpdatePerson {

        @Test
        void updateExistingPersonByUsername() {
            final String uniqueID = "uniqueID";
            final String givenName = "given name";
            final String familyName = "family name";
            final String email = "test.me@example.com";

            final AuthenticationSuccessEvent event = getOidcUserAuthority(Map.of(
                SUB, uniqueID,
                GIVEN_NAME, givenName,
                FAMILY_NAME, familyName,
                EMAIL, email
            ));

            final Person existingPerson = new Person(uniqueID, familyName, givenName, email);

            when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.of(existingPerson));

            sut.handle(event);

            verify(personService, never()).getPersonByMailAddress(email);

            final ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
            verify(personService).update(personArgumentCaptor.capture());

            Person update = personArgumentCaptor.getValue();
            assertThat(update.getUsername()).isEqualTo(uniqueID);
            assertThat(update.getLastName()).isEqualTo(familyName);
            assertThat(update.getFirstName()).isEqualTo(givenName);
            assertThat(update.getEmail()).isEqualTo(email);
        }

        @Test
        void updateExistingPersonByEmailFallback() {
            final String uniqueID = "uniqueID";
            final String givenName = "given name";
            final String familyName = "family name";
            final String email = "test.me@example.com";

            final AuthenticationSuccessEvent event = getOidcUserAuthority(Map.of(
                SUB, uniqueID,
                GIVEN_NAME, givenName,
                FAMILY_NAME, familyName,
                EMAIL, email
            ));

            final Person personForLogin = new Person();
            personForLogin.setUsername("idOfOtherIdentityProvider");
            personForLogin.setPermissions(List.of(USER));
            final Optional<Person> person = Optional.of(personForLogin);
            when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.empty());
            when(personService.getPersonByMailAddress(email)).thenReturn(person);


            sut.handle(event);

            verify(personService).getPersonByMailAddress(email);

            final ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
            verify(personService).update(personArgumentCaptor.capture());

            Person update = personArgumentCaptor.getValue();
            assertThat(update.getUsername()).isEqualTo(uniqueID);
            assertThat(update.getLastName()).isEqualTo(familyName);
            assertThat(update.getFirstName()).isEqualTo(givenName);
            assertThat(update.getEmail()).isEqualTo(email);
        }
    }

    @Nested
    class ExtractIdentifier {

        @Test
        void ensureFallbackToUserInfoIfIdentifierIsMissingInIdToken() {
            final String uniqueID = "uniqueID";
            final String givenName = "given name";
            final String familyName = "family name";
            final String email = "test.me@example.com";

            final AuthenticationSuccessEvent event = getOidcUserAuthority(
                Map.of(
                    FAMILY_NAME, familyName,
                    GIVEN_NAME, givenName,
                    EMAIL, email
                ),
                Map.of(
                    SUB, uniqueID
                )
            );

            when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.of(new Person(uniqueID, familyName, givenName, email)));

            sut.handle(event);

            verify(personService, never()).getPersonByMailAddress(email);

            final ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
            verify(personService).update(personArgumentCaptor.capture());

            Person update = personArgumentCaptor.getValue();
            assertThat(update.getUsername()).isEqualTo(uniqueID);
            assertThat(update.getLastName()).isEqualTo(familyName);
            assertThat(update.getFirstName()).isEqualTo(givenName);
            assertThat(update.getEmail()).isEqualTo(email);
        }
    }

    @Nested
    class ExtractEmail {

        @Test
        void ensureFallbackToUserInfoIfEmailIsMissingInIdToken() {
            final String uniqueID = "uniqueID";
            final String givenName = "given name";
            final String familyName = "family name";
            final String email = "test.me@example.com";

            final AuthenticationSuccessEvent event = getOidcUserAuthority(
                Map.of(
                    SUB, uniqueID,
                    FAMILY_NAME, familyName,
                    GIVEN_NAME, givenName
                ),
                Map.of(
                    EMAIL, email
                )
            );

            when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.of(new Person(uniqueID, familyName, givenName, email)));

            sut.handle(event);

            verify(personService, never()).getPersonByMailAddress(email);

            final ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
            verify(personService).update(personArgumentCaptor.capture());

            Person update = personArgumentCaptor.getValue();
            assertThat(update.getUsername()).isEqualTo(uniqueID);
            assertThat(update.getLastName()).isEqualTo(familyName);
            assertThat(update.getFirstName()).isEqualTo(givenName);
            assertThat(update.getEmail()).isEqualTo(email);
        }
    }

    @Nested
    class ExtractFamilyname {

        @Test
        void ensureFallbackToUserInfoIfFamilyNameIsMissingInIdToken() {
            final String uniqueID = "uniqueID";
            final String givenName = "given name";
            final String familyName = "family name";
            final String email = "test.me@example.com";

            final AuthenticationSuccessEvent event = getOidcUserAuthority(
                Map.of(
                    SUB, uniqueID,
                    GIVEN_NAME, givenName,
                    EMAIL, email
                ),
                Map.of(
                    FAMILY_NAME, familyName
                )
            );

            when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.of(new Person(uniqueID, familyName, givenName, email)));

            sut.handle(event);

            verify(personService, never()).getPersonByMailAddress(email);

            final ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
            verify(personService).update(personArgumentCaptor.capture());

            Person update = personArgumentCaptor.getValue();
            assertThat(update.getUsername()).isEqualTo(uniqueID);
            assertThat(update.getLastName()).isEqualTo(familyName);
            assertThat(update.getFirstName()).isEqualTo(givenName);
            assertThat(update.getEmail()).isEqualTo(email);
        }

        @Test
        void ensureThrowsExceptionIfFamilyNameIsMissing() {
            final AuthenticationSuccessEvent event = getOidcUserAuthority(Map.of(
                SUB, "uniqueID",
                GIVEN_NAME, "given name",
                EMAIL, "test.me@example.com"
            ));

            assertThatThrownBy(() -> sut.handle(event))
                .isInstanceOf(OidcPersonMappingException.class)
                .hasMessage("Can not retrieve the family name for oidc person mapping");
        }
    }

    @Nested
    class ExtractGivenName {
        @Test
        void ensureFallbackToUserInfoIfGivenNameIsMissingInIdToken() {
            final String uniqueID = "uniqueID";
            final String givenName = "given name";
            final String familyName = "family name";
            final String email = "test.me@example.com";

            final AuthenticationSuccessEvent event = getOidcUserAuthority(
                Map.of(
                    SUB, uniqueID,
                    FAMILY_NAME, familyName,
                    EMAIL, email
                ),
                Map.of(
                    GIVEN_NAME, givenName
                )
            );

            when(personService.getPersonByUsername(uniqueID)).thenReturn(Optional.of(new Person(uniqueID, familyName, givenName, email)));

            sut.handle(event);

            verify(personService, never()).getPersonByMailAddress(email);

            final ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
            verify(personService).update(personArgumentCaptor.capture());

            Person update = personArgumentCaptor.getValue();
            assertThat(update.getUsername()).isEqualTo(uniqueID);
            assertThat(update.getLastName()).isEqualTo(familyName);
            assertThat(update.getFirstName()).isEqualTo(givenName);
            assertThat(update.getEmail()).isEqualTo(email);
        }

        @Test
        void ensureThrowsExceptionIfGivenNameIsMissing() {
            final AuthenticationSuccessEvent event = getOidcUserAuthority(Map.of(
                SUB, "uniqueID",
                FAMILY_NAME, "family name",
                EMAIL, "test.me@example.com"
            ));

            assertThatThrownBy(() -> sut.handle(event))
                .isInstanceOf(OidcPersonMappingException.class)
                .hasMessage("Can not retrieve the given name for oidc person mapping");
        }
    }

    @Test
    void ensureToDoNothingWithJwtAsPrinciple() {

        final Authentication authentication = mock(Authentication.class);
        final Jwt jwt = mock(Jwt.class);
        when(authentication.getPrincipal()).thenReturn(jwt);

        final AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

        sut.handle(event);

        verifyNoInteractions(personService);
    }

    private AuthenticationSuccessEvent getOidcUserAuthority(Map<String, Object> idTokenClaims) {
        final OidcIdToken idToken = new OidcIdToken("tokenValue", Instant.now(), Instant.MAX, idTokenClaims);
        return createEvent(new DefaultOidcUser(null, idToken));
    }

    private AuthenticationSuccessEvent getOidcUserAuthority(List<GrantedAuthority> authorities, Map<String, Object> idTokenClaims) {
        final OidcIdToken idToken = new OidcIdToken("tokenValue", Instant.now(), Instant.MAX, idTokenClaims);
        return createEventWithAuthentication(new DefaultOidcUser(authorities, idToken));
    }

    private AuthenticationSuccessEvent getOidcUserAuthority(Map<String, Object> idTokenClaims, Map<String, Object> userInfoClaims) {
        final OidcIdToken idToken = new OidcIdToken("tokenValue", Instant.now(), Instant.MAX, idTokenClaims);
        final OidcUserInfo userInfo = new OidcUserInfo(userInfoClaims);
        return createEvent(new DefaultOidcUser(null, idToken, userInfo));
    }

    private AuthenticationSuccessEvent createEvent(DefaultOidcUser defaultOidcUser) {
        final OAuth2LoginAuthenticationToken oAuth2LoginAuthenticationToken = mock(OAuth2LoginAuthenticationToken.class);
        when(oAuth2LoginAuthenticationToken.getPrincipal()).thenReturn(defaultOidcUser);
        return new AuthenticationSuccessEvent(oAuth2LoginAuthenticationToken);
    }

    private AuthenticationSuccessEvent createEventWithAuthentication(DefaultOidcUser defaultOidcUser) {
        final OAuth2LoginAuthenticationToken oAuth2LoginAuthenticationToken = mock(OAuth2LoginAuthenticationToken.class);
        when(oAuth2LoginAuthenticationToken.getPrincipal()).thenReturn(defaultOidcUser);
        return new AuthenticationSuccessEvent(oAuth2LoginAuthenticationToken);
    }
}
