package org.synyx.urlaubsverwaltung.person.api;

import org.springframework.hateoas.ResourceSupport;
import org.synyx.urlaubsverwaltung.person.Person;


public class PersonResponse extends ResourceSupport {

    private String email;

    private String firstName;

    private String lastName;

    private String niceName;

    public PersonResponse(Person person) {

        this.email = person.getEmail();
        this.firstName = person.getFirstName();
        this.lastName = person.getLastName();
        this.niceName = person.getNiceName();
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


    public String getNiceName() {

        return niceName;
    }


    public void setNiceName(String niceName) {

        this.niceName = niceName;
    }
}
