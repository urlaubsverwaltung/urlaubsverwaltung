package org.synyx.urlaubsverwaltung.security;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;


/**
 * Represents a LDAP user with relevant information.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Entry(objectClasses = { "User" })
public final class LdapUser {

    @Id
    private Name dn;

    // TODO: Attribute names should be configurable!
    @Attribute(name = "uid")
//    @Attribute(name = "cn")
    private String username;

    @Attribute(name = "givenName")
    private String firstName;

    @Attribute(name = "sn")
    private String lastName;

    @Attribute(name = "mail")
    private String email;

    public Name getDn() {

        return dn;
    }


    public String getUsername() {

        return username;
    }


    public String getFirstName() {

        return firstName;
    }


    public String getLastName() {

        return lastName;
    }


    public String getEmail() {

        return email;
    }


    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
