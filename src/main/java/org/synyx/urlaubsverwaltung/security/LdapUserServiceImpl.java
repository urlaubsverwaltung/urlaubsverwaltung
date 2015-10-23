package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Conditional;

import org.springframework.ldap.core.LdapTemplate;

import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Conditional(LdapOrActiveDirectoryAuthenticationCondition.class)
public class LdapUserServiceImpl implements LdapUserService {

    private static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";

    private final LdapTemplate ldapTemplate;
    private final LdapUserMapper ldapUserMapper;
    private final String objectClass;

    @Autowired
    public LdapUserServiceImpl(LdapTemplate ldapTemplate, LdapUserMapper ldapUserMapper,
        @Value("${security.objectClass}") String objectClass) {

        this.ldapTemplate = ldapTemplate;
        this.ldapUserMapper = ldapUserMapper;
        this.objectClass = objectClass;
    }

    @Override
    public List<LdapUser> getLdapUsers() {

        return ldapTemplate.search(query().where(OBJECT_CLASS_ATTRIBUTE).is(objectClass), ldapUserMapper);
    }
}
