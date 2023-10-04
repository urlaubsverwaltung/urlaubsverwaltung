package org.synyx.urlaubsverwaltung.security.oidc;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

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
}
