package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.synyx.urlaubsverwaltung.security.SecurityConfigurationProperties;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.springframework.util.StringUtils.hasText;


/**
 * Maps LDAP attributes to {@link LdapUser} class.
 */
public class LdapUserMapper implements AttributesMapper<LdapUser> {

    private static final String MEMBER_OF_ATTRIBUTE = "memberOf";

    private final SecurityConfigurationProperties securityProperties;

    LdapUserMapper(SecurityConfigurationProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public LdapUser mapFromAttributes(Attributes attributes) throws NamingException {

        Optional<Attribute> userNameAttribute = Optional.ofNullable(attributes.get(securityProperties.getIdentifier()));

        if (!userNameAttribute.isPresent()) {
            throw new InvalidSecurityConfigurationException("User identifier is configured incorrectly");
        }

        String username = (String) userNameAttribute.get().get();

        Optional<String> firstName = getAttributeValue(attributes, securityProperties.getFirstName());
        Optional<String> lastName = getAttributeValue(attributes, securityProperties.getLastName());
        Optional<String> email = getAttributeValue(attributes, securityProperties.getMailAddress());

        List<String> groups = new ArrayList<>();
        Optional<Attribute> memberOfAttribute = Optional.ofNullable(attributes.get(MEMBER_OF_ATTRIBUTE));

        if (memberOfAttribute.isPresent()) {
            NamingEnumeration<?> groupNames = memberOfAttribute.get().getAll();

            while (groupNames.hasMoreElements()) {
                groups.add((String) groupNames.nextElement());
            }
        }

        return new LdapUser(username, firstName, lastName, email, groups.toArray(new String[0]));
    }


    private Optional<String> getAttributeValue(Attributes attributes, String attributeName) throws NamingException {

        Optional<Attribute> attribute = Optional.ofNullable(attributes.get(attributeName));
        Optional<String> attributeValue = Optional.empty();

        if (attribute.isPresent()) {
            attributeValue = Optional.ofNullable((String) attribute.get().get());
        }

        return attributeValue;
    }


    LdapUser mapFromContext(DirContextOperations ctx) throws UnsupportedMemberAffiliationException {

        String username = Optional.ofNullable(ctx.getStringAttribute(securityProperties.getIdentifier())).orElseThrow(() ->
                new InvalidSecurityConfigurationException(
                    "Can not get a username using '" + securityProperties.getIdentifier() + "' attribute to identify the user."));

        Optional<String> firstName = Optional.ofNullable(ctx.getStringAttribute(securityProperties.getFirstName()));
        Optional<String> lastName = Optional.ofNullable(ctx.getStringAttribute(securityProperties.getLastName()));
        Optional<String> email = Optional.ofNullable(ctx.getStringAttribute(securityProperties.getMailAddress()));

        if (hasText(securityProperties.getFilter().getMemberOf())) {
            String[] memberOf = ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE);

            if (!asList(memberOf).contains(securityProperties.getFilter().getMemberOf())) {
                throw new UnsupportedMemberAffiliationException("User '" + username + "' is not a member of '"
                    + securityProperties.getFilter().getMemberOf() + "'");
            }

            return new LdapUser(username, firstName, lastName, email, memberOf);
        }

        return new LdapUser(username, firstName, lastName, email);
    }
}
