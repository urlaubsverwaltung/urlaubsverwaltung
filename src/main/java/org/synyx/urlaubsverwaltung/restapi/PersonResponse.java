package org.synyx.urlaubsverwaltung.restapi;

import org.synyx.urlaubsverwaltung.person.Person;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
class PersonResponse {

    private String ldapName;

    private String email;

    private String firstName;

    private String lastName;

    PersonResponse(Person person) {

        this.ldapName = person.getLoginName();
        this.email = person.getEmail();
        this.firstName = person.getFirstName();
        this.lastName = person.getLastName();
    }

    public String getLdapName() {

        return ldapName;
    }


    public void setLdapName(String ldapName) {

        this.ldapName = ldapName;
    }


    public String getEmail() {

        return email;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    public String getFirstName() {

        return firstName;
    }


    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }


    public String getLastName() {

        return lastName;
    }


    public void setLastName(String lastName) {

        this.lastName = lastName;
    }
}
