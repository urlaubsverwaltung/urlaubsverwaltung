package org.synyx.urlaubsverwaltung.security.oidc;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("uv.security.oidc.claim-mappers")
public class RolesFromClaimMappersProperties {


    /**
     * Activates or deactivates the authority check of the claim mappers, if they are enabled.
     * When the authority check is enabled, then you need to provide the {@link RolesFromClaimMappersProperties#rolePrefix}user authority in your iam,
     * otherwise it can be done via the ui in the application.
     *
     */
    private boolean authorityCheckEnabled = true;

    /**
     * Prefix to compare the defined roles/groups against. This role prefix is case-sensitive.
     */
    @NotEmpty
    private String rolePrefix = "urlaubsverwaltung_";

    @Valid
    @NotNull
    private ResourceAccessClaimMapperProperties resourceAccessClaim = new ResourceAccessClaimMapperProperties();

    @Valid
    @NotNull
    private GroupClaimMapperProperties groupClaim = new GroupClaimMapperProperties();

    public boolean isAuthorityCheckEnabled() {
        return authorityCheckEnabled;
    }

    public void setAuthorityCheckEnabled(boolean authorityCheckEnabled) {
        this.authorityCheckEnabled = authorityCheckEnabled;
    }

    public String getRolePrefix() {
        return rolePrefix;
    }

    public void setRolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }

    public ResourceAccessClaimMapperProperties getResourceAccessClaim() {
        return resourceAccessClaim;
    }

    public void setResourceAccessClaim(ResourceAccessClaimMapperProperties resourceAccessClaim) {
        this.resourceAccessClaim = resourceAccessClaim;
    }

    public GroupClaimMapperProperties getGroupClaim() {
        return groupClaim;
    }

    public void setGroupClaim(GroupClaimMapperProperties groupClaim) {
        this.groupClaim = groupClaim;
    }

    public static class ResourceAccessClaimMapperProperties {

        private boolean enabled = false;

        @NotEmpty
        private String resourceApp = "urlaubsverwaltung";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getResourceApp() {
            return resourceApp;
        }

        public void setResourceApp(String resourceApp) {
            this.resourceApp = resourceApp;
        }
    }

    public static class GroupClaimMapperProperties {

        private boolean enabled = false;

        @NotEmpty
        private String claimName = "groups";

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
    }
}
