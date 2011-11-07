/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.domain;

import org.joda.time.DateMidnight;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;


/**
 * @author  aljona
 */
@Entity
public class Kommentar extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1L;

    private String text;

    private Person person;

    private DateMidnight datum;

    public DateMidnight getDatum() {

        return datum;
    }


    public void setDatum(DateMidnight datum) {

        this.datum = datum;
    }


    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public String getText() {

        return text;
    }


    public void setText(String text) {

        this.text = text;
    }
}
