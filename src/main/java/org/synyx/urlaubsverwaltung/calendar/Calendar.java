package org.synyx.urlaubsverwaltung.calendar;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;

@Entity
@Inheritance
abstract class Calendar {

    private static final int SECRET_LENGTH = 32;

    @Id
    @GeneratedValue
    private Long id;

    @Length(min = SECRET_LENGTH, max = SECRET_LENGTH)
    private String secret;

    public Calendar() {
        generateSecret();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void generateSecret() {
        secret = RandomStringUtils.randomAlphanumeric(SECRET_LENGTH);
    }
}
