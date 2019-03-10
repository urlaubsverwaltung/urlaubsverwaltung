package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@ConditionalOnExpression(
    "('${auth}'=='activeDirectory' and '${uv.security.activeDirectory.sync}'=='true') or ('${auth}'=='ldap' and '${uv.security.ldap.sync}'=='true')" // NOSONAR
)
public class LdapUserServiceImpl implements LdapUserService {

    private static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";
    private static final String MEMBER_OF_ATTRIBUTE = "memberOf";

    private final LdapTemplate ldapTemplate;
    private final LdapUserMapper ldapUserMapper;
    private final String objectClass;
    private final String memberOf;

    @Autowired
    public LdapUserServiceImpl(LdapTemplate ldapTemplate, LdapUserMapper ldapUserMapper,
        @Value("${uv.security.filter.objectClass}") String objectClass,
        @Value("${uv.security.filter.memberOf}") String memberOf) {

        this.ldapTemplate = ldapTemplate;
        this.ldapUserMapper = ldapUserMapper;
        this.objectClass = objectClass;
        this.memberOf = memberOf;
    }

    @Override
    public List<LdapUser> getLdapUsers() {

        if (StringUtils.hasText(memberOf)) {
            return ldapTemplate.search(query().where(OBJECT_CLASS_ATTRIBUTE)
                    .is(objectClass)
                    .and(MEMBER_OF_ATTRIBUTE)
                    .is(memberOf), ldapUserMapper);
        }

        return ldapTemplate.search(query().where(OBJECT_CLASS_ATTRIBUTE).is(objectClass), ldapUserMapper);
    }
}
