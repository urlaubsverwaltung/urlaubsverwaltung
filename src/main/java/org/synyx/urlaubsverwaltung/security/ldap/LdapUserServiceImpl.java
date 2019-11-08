package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.ldap.core.LdapTemplate;

import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;
import static org.springframework.util.StringUtils.hasText;


public class LdapUserServiceImpl implements LdapUserService {

    private static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";
    private static final String MEMBER_OF_ATTRIBUTE = "memberOf";

    private final LdapTemplate ldapTemplate;
    private final LdapUserMapper ldapUserMapper;
    private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;

    LdapUserServiceImpl(LdapTemplate ldapTemplate, LdapUserMapper ldapUserMapper,
                        DirectoryServiceSecurityProperties directoryServiceSecurityProperties) {

        this.ldapTemplate = ldapTemplate;
        this.ldapUserMapper = ldapUserMapper;
        this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
    }

    @Override
    public List<LdapUser> getLdapUsers() {

        final String memberOf = directoryServiceSecurityProperties.getFilter().getMemberOf();
        final String objectClass = directoryServiceSecurityProperties.getFilter().getObjectClass();

        if (hasText(memberOf)) {
            return ldapTemplate.search(query().where(OBJECT_CLASS_ATTRIBUTE)
                .is(objectClass)
                .and(MEMBER_OF_ATTRIBUTE)
                .is(memberOf), ldapUserMapper);
        }

        return ldapTemplate.search(query().where(OBJECT_CLASS_ATTRIBUTE).is(objectClass), ldapUserMapper);
    }
}
