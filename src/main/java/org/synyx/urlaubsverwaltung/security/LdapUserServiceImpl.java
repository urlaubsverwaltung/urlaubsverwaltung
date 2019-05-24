package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.security.config.SecurityConfigurationProperties;

import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;
import static org.springframework.util.StringUtils.hasText;


@Service
@ConditionalOnExpression(
    "('${auth}'=='activeDirectory' and '${uv.security.activeDirectory.sync}'=='true') or ('${auth}'=='ldap' and '${uv.security.ldap.sync}'=='true')" // NOSONAR
)
public class LdapUserServiceImpl implements LdapUserService {

    private static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";
    private static final String MEMBER_OF_ATTRIBUTE = "memberOf";

    private final LdapTemplate ldapTemplate;
    private final LdapUserMapper ldapUserMapper;
    private final SecurityConfigurationProperties securityProperties;

    @Autowired
    public LdapUserServiceImpl(LdapTemplate ldapTemplate, LdapUserMapper ldapUserMapper,
                               SecurityConfigurationProperties securityProperties) {

        this.ldapTemplate = ldapTemplate;
        this.ldapUserMapper = ldapUserMapper;
        this.securityProperties = securityProperties;
    }

    @Override
    public List<LdapUser> getLdapUsers() {

        final String memberOf = securityProperties.getFilter().getMemberOf();
        final String objectClass = securityProperties.getFilter().getObjectClass();

        if (hasText(memberOf)) {
            return ldapTemplate.search(query().where(OBJECT_CLASS_ATTRIBUTE)
                    .is(objectClass)
                    .and(MEMBER_OF_ATTRIBUTE)
                    .is(memberOf), ldapUserMapper);
        }

        return ldapTemplate.search(query().where(OBJECT_CLASS_ATTRIBUTE).is(objectClass), ldapUserMapper);
    }
}
