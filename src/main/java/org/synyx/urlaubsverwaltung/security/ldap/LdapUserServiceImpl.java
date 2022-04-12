package org.synyx.urlaubsverwaltung.security.ldap;

import org.slf4j.Logger;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;

import java.util.ArrayList;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
import static org.springframework.util.StringUtils.hasText;

public class LdapUserServiceImpl implements LdapUserService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

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

        List<LdapUser> search = new ArrayList<>();
        try {
            search = ldapTemplate.search(criteria, ldapUserMapper);
        } catch (InvalidSecurityConfigurationException e) {
            LOG.error("Could not perform a search with parameters from the specified LdapQuery with objectClass '{}' and memberOf '{}'", objectClass, memberOf, e);
        }

        return search;
    }
}
