package org.synyx.urlaubsverwaltung.person.api;

import org.springframework.hateoas.RepresentationModel;

public class PersonResponse extends RepresentationModel {

    private String email;
    private String firstName;
    private String lastName;
    private String niceName;

    PersonResponse(String email, String firstName, String lastName, String niceName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.niceName = niceName;
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
