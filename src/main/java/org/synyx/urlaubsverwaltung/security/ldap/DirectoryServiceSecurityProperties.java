package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.synyx.urlaubsverwaltung.validation.CronExpression;

import javax.validation.Valid;

@Component
@ConfigurationProperties("uv.security.directory-service")
@Validated
public class DirectoryServiceSecurityProperties {

    private String identifier;
    private String firstName;
    private String lastName;
    private String mailAddress;
    private SecurityFilter filter = new SecurityFilter();

    @Valid
    private SecuritySync sync = new SecuritySync();

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public SecurityFilter getFilter() {
        return filter;
    }

    public void setFilter(SecurityFilter filter) {
        this.filter = filter;
    }

    public SecuritySync getSync() {
        return sync;
    }

    public void setSync(SecuritySync sync) {
        this.sync = sync;
    }

    public static class SecurityFilter {

        private String objectClass;
        private String memberOf;

        public String getObjectClass() {
            return objectClass;
        }

        public void setObjectClass(String objectClass) {
            this.objectClass = objectClass;
        }

        public String getMemberOf() {
            return memberOf;
        }

        public void setMemberOf(String memberOf) {
            this.memberOf = memberOf;
        }
    }

    public static class SecuritySync {

        /**
         * Syncs directory services data by default every night at 01:00 am
         * if `ldap` or `activedirectory` is activated via `uv.security.auth`
         */
        @CronExpression
        private String cron = "0 0 1 * * ?";

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }
    }
}

