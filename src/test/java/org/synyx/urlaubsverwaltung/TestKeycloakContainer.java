package org.synyx.urlaubsverwaltung;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.test.context.DynamicPropertyRegistry;

import static org.keycloak.admin.client.CreatedResponseUtil.getCreatedId;

public class TestKeycloakContainer extends KeycloakContainer {

    private static final String VERSION = "26.0.6";
    private static final String IMAGE = "quay.io/keycloak/keycloak";
    private static final String REALM_URLAUBSVERWALTUNG = "urlaubsverwaltung";

    public TestKeycloakContainer() {
        super(IMAGE + ":" + VERSION);
        this.withRealmImportFiles("/docker/keycloak-export/urlaubsverwaltung-realm.json");
    }

    public String createUser(String username, String firstName, String lastName, String email, String secret) {

        // Define user
        final UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setEmailVerified(true);

        final Keycloak keycloak = this.adminClient();

        // Get realm
        final RealmResource realmResource = keycloak.realm(REALM_URLAUBSVERWALTUNG);
        final UsersResource usersRessource = realmResource.users();

        // Create user (requires manage-users role)
        final Response response = usersRessource.create(user);

        // Define password credential
        final CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(secret);

        // Set password credential
        final UserResource userResource = usersRessource.get(getCreatedId(response));
        userResource.resetPassword(passwordCred);

        return userResource.toRepresentation().getId();
    }

    /**
     * Sets the spring security client configuration properties.
     *
     * <p>Usage:</p>
     * <pre><code>
     * static final TestKeycloakContainer keycloak = new TestKeycloakContainer();
     * &#64;DynamicPropertySource
     * static void setupDataSource(DynamicPropertySource registry) {
     *     keycloak.start();
     *     keycloak.configureSpringDataSource(registry);
     * }
     * </code>
     * </pre>
     *
     * @param registry {@link DynamicPropertyRegistry} to configure configurations dynamically
     */
    public final void configureSpringDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.client.registration.keycloak.client-id", () -> "urlaubsverwaltung");
        registry.add("spring.security.oauth2.client.registration.keycloak.client-secret", () -> "urlaubsverwaltung-secret");
        registry.add("spring.security.oauth2.client.registration.keycloak.provider", () -> "keycloak");
        registry.add("spring.security.oauth2.client.registration.keycloak.scope", () -> "openid,profile,email,roles");
        registry.add("spring.security.oauth2.client.registration.keycloak.authorization-grant-type", () -> "authorization_code");
        registry.add("spring.security.oauth2.client.registratirealmon.keycloak.redirect-uri", () -> "http://{baseHost}{basePort}/login/oauth2/code/{registrationId}");
        registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri", () -> this.getAuthServerUrl() + "/realms/"+ REALM_URLAUBSVERWALTUNG);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> this.getAuthServerUrl() + "/realms/"+ REALM_URLAUBSVERWALTUNG);
    }

    private Keycloak adminClient() {
        return KeycloakBuilder.builder()
            .serverUrl(this.getAuthServerUrl())
            .realm("master")
            .clientId("admin-cli")
            .grantType(OAuth2Constants.PASSWORD)
            .username(this.getAdminUsername())
            .password(this.getAdminPassword())
            .build();
    }
}
