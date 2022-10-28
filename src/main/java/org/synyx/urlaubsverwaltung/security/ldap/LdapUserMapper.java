package org.synyx.urlaubsverwaltung.security.ldap;

import org.slf4j.Logger;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.hasText;

/**
 * Maps LDAP attributes to {@link LdapUser} class.
 */
public class LdapUserMapper implements AttributesMapper<LdapUser> {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;

    LdapUserMapper(DirectoryServiceSecurityProperties directoryServiceSecurityProperties) {
        this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
    }

    @Override
    public LdapUser mapFromAttributes(Attributes attributes) throws NamingException {

        final String username = extractAttribute(attributes, directoryServiceSecurityProperties.getIdentifier());
        final String firstName = extractAttribute(attributes, directoryServiceSecurityProperties.getFirstName());
        final String lastName = extractAttribute(attributes, directoryServiceSecurityProperties.getLastName());
        final String email = extractAttribute(attributes, directoryServiceSecurityProperties.getMailAddress());

        final List<String> groups = new ArrayList<>();
        final Attribute memberOfAttribute = attributes.get("memberOf");
        if (memberOfAttribute != null) {
            final NamingEnumeration<?> groupNames = memberOfAttribute.getAll();
            while (groupNames.hasMoreElements()) {
                groups.add((String) groupNames.nextElement());
            }
        }

        return new LdapUser(username, firstName, lastName, email, groups);
    }

    LdapUser mapFromContext(DirContextOperations ctx) throws UnsupportedMemberAffiliationException {

        final String username = extractAttribute(ctx, directoryServiceSecurityProperties.getIdentifier());
        final String firstName = extractAttribute(ctx, directoryServiceSecurityProperties.getFirstName());
        final String lastName = extractAttribute(ctx, directoryServiceSecurityProperties.getLastName());
        final String email = extractAttribute(ctx, directoryServiceSecurityProperties.getMailAddress());

        List<String> memberOf = new ArrayList<>();
        final String memberOfProperty = directoryServiceSecurityProperties.getFilter().getMemberOf();
        if (hasText(memberOfProperty)) {
            memberOf = asList(ctx.getStringAttributes("memberOf"));

            if (!memberOf.contains(memberOfProperty)) {
                LOG.error("User '{}' is not a member of '{}'", username, memberOfProperty);
                throw new UnsupportedMemberAffiliationException("User '" + username + "' is not a member of '" + memberOfProperty + "'");
            }
        }

        return new LdapUser(username, firstName, lastName, email, memberOf);
    }

    private String extractAttribute(Attributes attributes, String identifier) throws NamingException {
        final Attribute attribute = attributes.get(identifier);
        if (attribute == null) {
            LOG.error("Can not retrieve the attribute using the identifier '{}'", identifier);
            throw new InvalidSecurityConfigurationException("Can not retrieve the attribute using the identifier '" + identifier + "'");
        }

        final String attributeValue = (String) attribute.get();
        if (attributeValue == null || attributeValue.isBlank()) {
            LOG.error("The attribute using the identifier '{}' is blank or null", identifier);
            throw new InvalidSecurityConfigurationException("The attribute using the identifier '" + identifier + "' is blank or null");
        }

        return attributeValue;
    }

    private String extractAttribute(DirContextOperations dirContextOperations, String identifier) {
        final String attribute = dirContextOperations.getStringAttribute(identifier);
        if (attribute == null || attribute.isBlank()) {
            LOG.error("The attribute using the identifier '{}' is blank or null", identifier);
            throw new InvalidSecurityConfigurationException("The attribute using the identifier '" + identifier + "' is blank or null");
        }
        return attribute;
    }
}
