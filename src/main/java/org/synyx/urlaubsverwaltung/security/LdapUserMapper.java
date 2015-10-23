package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;

import org.springframework.stereotype.Component;

import java.util.Optional;

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

    private final String identifierAttribute;
    private final String firstNameAttribute;
    private final String lastNameAttribute;
    private final String mailAddressAttribute;

    @Autowired
    public LdapUserMapper(@Value("${security.identifier}") String identifierAttribute,
        @Value("${security.firstName}") String firstNameAttribute,
        @Value("${security.lastName}") String lastNameAttribute,
        @Value("${security.mailAddress}") String mailAddressAttribute) {

        this.identifierAttribute = identifierAttribute;
        this.firstNameAttribute = firstNameAttribute;
        this.lastNameAttribute = lastNameAttribute;
        this.mailAddressAttribute = mailAddressAttribute;
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

        return new LdapUser(username, firstName, lastName, email);
    }


    private Optional<String> getAttributeValue(Attributes attributes, String attributeName) throws NamingException {

        Optional<Attribute> attribute = Optional.ofNullable(attributes.get(attributeName));
        Optional<String> attributeValue = Optional.empty();

        if (attribute.isPresent()) {
            attributeValue = Optional.ofNullable((String) attribute.get().get());
        }

        return attributeValue;
    }


    public LdapUser mapFromContext(DirContextOperations ctx) {

        Optional.ofNullable(ctx.getStringAttribute(identifierAttribute)).orElseThrow(() ->
                new InvalidSecurityConfigurationException(
                    "Can not get a username using '" + identifierAttribute + "' attribute to identify the user."));

        String username = ctx.getStringAttribute(identifierAttribute);

        Optional<String> firstName = Optional.ofNullable(ctx.getStringAttribute(firstNameAttribute));
        Optional<String> lastName = Optional.ofNullable(ctx.getStringAttribute(lastNameAttribute));
        Optional<String> email = Optional.ofNullable(ctx.getStringAttribute(mailAddressAttribute));

        return new LdapUser(username, firstName, lastName, email);
    }
}
