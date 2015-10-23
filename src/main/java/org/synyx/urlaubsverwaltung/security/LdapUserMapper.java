package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;


/**
 * Maps LDAP attributes to {@link LdapUser} class.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
public class LdapUserMapper implements AttributesMapper<LdapUser> {

    private static final String MEMBER_OF_ATTRIBUTE = "memberOf";

    private final String identifierAttribute;
    private final String firstNameAttribute;
    private final String lastNameAttribute;
    private final String mailAddressAttribute;

    private final String memberOfFilter;

    @Autowired
    public LdapUserMapper(@Value("${security.identifier}") String identifierAttribute,
        @Value("${security.firstName}") String firstNameAttribute,
        @Value("${security.lastName}") String lastNameAttribute,
        @Value("${security.mailAddress}") String mailAddressAttribute,
        @Value("${security.filter.memberOf}") String memberOfFilter) {

        this.identifierAttribute = identifierAttribute;
        this.firstNameAttribute = firstNameAttribute;
        this.lastNameAttribute = lastNameAttribute;
        this.mailAddressAttribute = mailAddressAttribute;
        this.memberOfFilter = memberOfFilter;
    }

    @Override
    public LdapUser mapFromAttributes(Attributes attributes) throws NamingException {

        Optional<Attribute> userNameAttribute = Optional.ofNullable(attributes.get(identifierAttribute));

        if (!userNameAttribute.isPresent()) {
            throw new InvalidSecurityConfigurationException("User identifier is configured incorrectly");
        }

        String username = (String) userNameAttribute.get().get();

        Optional<String> firstName = getAttributeValue(attributes, firstNameAttribute);
        Optional<String> lastName = getAttributeValue(attributes, lastNameAttribute);
        Optional<String> email = getAttributeValue(attributes, mailAddressAttribute);

        List<String> groups = new ArrayList<>();
        Optional<Attribute> memberOfAttribute = Optional.ofNullable(attributes.get(MEMBER_OF_ATTRIBUTE));

        if (memberOfAttribute.isPresent()) {
            NamingEnumeration<?> groupNames = memberOfAttribute.get().getAll();

            while (groupNames.hasMoreElements()) {
                groups.add((String) groupNames.nextElement());
            }
        }

        return new LdapUser(username, firstName, lastName, email, groups.stream().toArray(String[]::new));
    }


    private Optional<String> getAttributeValue(Attributes attributes, String attributeName) throws NamingException {

        Optional<Attribute> attribute = Optional.ofNullable(attributes.get(attributeName));
        Optional<String> attributeValue = Optional.empty();

        if (attribute.isPresent()) {
            attributeValue = Optional.ofNullable((String) attribute.get().get());
        }

        return attributeValue;
    }


    public LdapUser mapFromContext(DirContextOperations ctx) throws NamingException,
        UnsupportedMemberAffiliationException {

        Optional.ofNullable(ctx.getStringAttribute(identifierAttribute)).orElseThrow(() ->
                new InvalidSecurityConfigurationException(
                    "Can not get a username using '" + identifierAttribute + "' attribute to identify the user."));

        String username = ctx.getStringAttribute(identifierAttribute);

        Optional<String> firstName = Optional.ofNullable(ctx.getStringAttribute(firstNameAttribute));
        Optional<String> lastName = Optional.ofNullable(ctx.getStringAttribute(lastNameAttribute));
        Optional<String> email = Optional.ofNullable(ctx.getStringAttribute(mailAddressAttribute));

        if (StringUtils.hasText(memberOfFilter)) {
            String[] memberOf = ctx.getStringAttributes(MEMBER_OF_ATTRIBUTE);

            if (!Arrays.asList(memberOf).contains(memberOfFilter)) {
                throw new UnsupportedMemberAffiliationException("User '" + username + "' is not a member of '"
                    + memberOfFilter + "'");
            }

            return new LdapUser(username, firstName, lastName, email, memberOf);
        }

        return new LdapUser(username, firstName, lastName, email);
    }
}
