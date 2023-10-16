package org.synyx.urlaubsverwaltung.security.oidc;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import static org.springframework.security.oauth2.core.oidc.IdTokenClaimNames.SUB;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.FAMILY_NAME;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.GIVEN_NAME;

@Validated
@ConfigurationProperties("uv.security.oidc")
public class OidcSecurityProperties {

    /**
     * OIDC post logout redirect uri.
     * <p>
     * Redirects the user to the given url after logout.
     * Default is the base url of the request.
     */
    @NotEmpty
    private String postLogoutRedirectUri = "{baseUrl}";

    @NotEmpty
    private String loginFormUrl;

    @NotNull
    private GroupClaim groupClaim = new GroupClaim();

    @NotNull
    private UserMappings userMappings = new UserMappings();

    public String getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }

    public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }

    public String getLoginFormUrl() {
        return loginFormUrl;
    }

    public void setLoginFormUrl(String loginFormUrl) {
        this.loginFormUrl = loginFormUrl;
    }

    public GroupClaim getGroupClaim() {
        return groupClaim;
    }

    public void setGroupClaim(GroupClaim groupClaim) {
        this.groupClaim = groupClaim;
    }

    public UserMappings getUserMappings() {
        return userMappings;
    }

    public void setUserMappings(UserMappings userMappings) {
        this.userMappings = userMappings;
    }

    public static class GroupClaim {

        private boolean enabled = false;

        @NotEmpty
        private String claimName = "groups";

        @NotEmpty
        private String permittedGroup = "urlaubsverwaltung_user";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getClaimName() {
            return claimName;
        }

        public void setClaimName(String claimName) {
            this.claimName = claimName;
        }

        public String getPermittedGroup() {
            return permittedGroup;
        }

        public void setPermittedGroup(String permittedGroup) {
            this.permittedGroup = permittedGroup;
        }
    }

    public static class UserMappings {

        @NotEmpty
        private String identifier = SUB;
        @NotEmpty
        private String familyName = FAMILY_NAME;
        @NotEmpty
        private String givenName = GIVEN_NAME;
        @NotEmpty
        private String email = EMAIL;

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
