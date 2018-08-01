package org.synyx.urlaubsverwaltung.test;

import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.util.Collection;


public class PersonBuilder {

    private Person person;

    public PersonBuilder() {

        person = new Person();
    }

    public PersonBuilder build() {

        return this;
    }


    public PersonBuilder withName(String firstName, String lastName) {

        person.setFirstName(firstName);
        person.setLastName(lastName);

        return this;
    }


    public PersonBuilder withEmail(String email) {

        person.setEmail(email);

        return this;
    }


    public PersonBuilder withLoginName(String loginName) {

        person.setLoginName(loginName);

        return this;
    }


    public PersonBuilder withPermissions(Collection<Role> permissions) {

        person.setPermissions(permissions);

        return this;
    }


    public PersonBuilder withNotifications(Collection<MailNotification> notifications) {

        person.setNotifications(notifications);

        return this;
    }


    public Person get() {

        return person;
    }


    public Person then() {

        return person;
    }
}
