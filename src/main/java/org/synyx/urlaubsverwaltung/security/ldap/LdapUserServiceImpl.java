package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;

import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;
import static org.springframework.util.StringUtils.hasText;


public class LdapUserServiceImpl implements LdapUserService {

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

        final ContainerCriteria criteria = query().where("objectClass").is(objectClass);
        if (hasText(memberOf)) {
            criteria.and("memberOf").is(memberOf);
        }

        return ldapTemplate.search(criteria, ldapUserMapper);
    }
}
