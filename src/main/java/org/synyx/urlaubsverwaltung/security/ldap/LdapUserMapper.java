package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.springframework.util.StringUtils.hasText;


/**
 * Maps LDAP attributes to {@link LdapUser} class.
 */
public class LdapUserMapper implements AttributesMapper<LdapUser> {

    private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;

    LdapUserMapper(DirectoryServiceSecurityProperties directoryServiceSecurityProperties) {
        this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
    }

    @Override
    public LdapUser mapFromAttributes(Attributes attributes) throws NamingException {

        final Attribute userNameAttribute = attributes.get(directoryServiceSecurityProperties.getIdentifier());
        if (userNameAttribute == null) {
            throw new InvalidSecurityConfigurationException("User identifier is configured incorrectly");
        }

        final List<String> groups = new ArrayList<>();
        final Attribute memberOfAttribute = attributes.get("memberOf");
        if (memberOfAttribute != null) {
            final NamingEnumeration<?> groupNames = memberOfAttribute.getAll();
            while (groupNames.hasMoreElements()) {
                groups.add((String) groupNames.nextElement());
            }
        }

        final String username = (String) userNameAttribute.get();
        final String firstName = getAttributeValue(attributes, directoryServiceSecurityProperties.getFirstName());
        final String lastName = getAttributeValue(attributes, directoryServiceSecurityProperties.getLastName());
        final String email = getAttributeValue(attributes, directoryServiceSecurityProperties.getMailAddress());

        return new LdapUser(username, firstName, lastName, email, groups);
    }

    LdapUser mapFromContext(DirContextOperations ctx) throws UnsupportedMemberAffiliationException {

        final String identifier = directoryServiceSecurityProperties.getIdentifier();
        final String username = ctx.getStringAttribute(identifier);
        if (username == null) {
            throw new InvalidSecurityConfigurationException("Can not get a username using '" + identifier + "' attribute to identify the user.");
        }

        final String firstName = ctx.getStringAttribute(directoryServiceSecurityProperties.getFirstName());
        final String lastName = ctx.getStringAttribute(directoryServiceSecurityProperties.getLastName());
        final String email = ctx.getStringAttribute(directoryServiceSecurityProperties.getMailAddress());

        List<String> memberOf = new ArrayList<>();
        final String memberOfProperty = directoryServiceSecurityProperties.getFilter().getMemberOf();
        if (hasText(memberOfProperty)) {
            memberOf = asList(ctx.getStringAttributes("memberOf"));

            if (!memberOf.contains(memberOfProperty)) {
                throw new UnsupportedMemberAffiliationException("User '" + username + "' is not a member of '" + memberOfProperty + "'");
            }
        }

        return new LdapUser(username, firstName, lastName, email, memberOf);
    }

    private String getAttributeValue(Attributes attributes, String attrID) throws NamingException {

        String attributeValue = null;

        final Attribute attribute = attributes.get(attrID);
        if (attribute != null) {
            attributeValue = (String) attribute.get();
        }

        return attributeValue;
    }
}
