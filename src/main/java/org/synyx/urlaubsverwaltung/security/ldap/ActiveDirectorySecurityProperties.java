package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("uv.security.directory-service.active-directory")
public class ActiveDirectorySecurityProperties {

    private String domain;
    private String url;

    private SecurityActiveDirectorySync sync = new SecurityActiveDirectorySync();

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SecurityActiveDirectorySync getSync() {
        return sync;
    }

    public void setSync(SecurityActiveDirectorySync sync) {
        this.sync = sync;
    }

    public static class SecurityActiveDirectorySync {

        private boolean enabled;
        private String userSearchBase;
        private String userDn;
        private String password;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUserSearchBase() {
            return userSearchBase;
        }

        public void setUserSearchBase(String userSearchBase) {
            this.userSearchBase = userSearchBase;
        }

        public String getUserDn() {
            return userDn;
        }

        public void setUserDn(String userDn) {
            this.userDn = userDn;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

