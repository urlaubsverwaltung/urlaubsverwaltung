package org.synyx.urlaubsverwaltung.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("uv.security")
public class SecurityConfigurationProperties {

    private String identifier;
    private String firstName;
    private String lastName;
    private String mailAddress;
    private SecurityFilter filter = new SecurityFilter();

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
}

