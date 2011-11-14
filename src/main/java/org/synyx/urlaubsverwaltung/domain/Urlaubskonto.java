/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


/**
 * @author  aljona
 */
@Entity
public class Urlaubskonto extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1L;

    // Eine Person kann mehrere Urlaubskonten besitzen (je nach Jahr)
    @ManyToOne
    private Person person;

    // Jahr des Urlaubskontos
    private Integer year;

    // wieviel darf person noch nehmen im jahr year
    // uebrige Urlaubstage, die noch genommen werden duerfen im Jahr year
    private Integer vacationDays;

    // verbleibender resturlaub im jahr year
    private Integer restVacationDays;

    // in diesem Jahr benutzter normal-Urlaub
    private Integer usedVacationDays;

    // in diesem Jahr benutzter resturlaub
    private Integer usedRestVacationDays;

    // genommener sonderurlaub im jahr year
    private Integer sonderUrlaub;

    // genommener unbezahlter urlaub im jahr year
    private Integer unbezahlterUrlaub;

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public Integer getRestVacationDays() {

        return restVacationDays;
    }


    public void setRestVacationDays(Integer restVacationDays) {

        this.restVacationDays = restVacationDays;
    }


    public Integer getSonderUrlaub() {

        return sonderUrlaub;
    }


    public void setSonderUrlaub(Integer sonderUrlaub) {

        this.sonderUrlaub = sonderUrlaub;
    }


    public Integer getUnbezahlterUrlaub() {

        return unbezahlterUrlaub;
    }


    public void setUnbezahlterUrlaub(Integer unbezahlterUrlaub) {

        this.unbezahlterUrlaub = unbezahlterUrlaub;
    }


    public Integer getUsedRestVacationDays() {

        return usedRestVacationDays;
    }


    public void setUsedRestVacationDays(Integer usedRestVacationDays) {

        this.usedRestVacationDays = usedRestVacationDays;
    }


    public Integer getUsedVacationDays() {

        return usedVacationDays;
    }


    public void setUsedVacationDays(Integer usedVacationDays) {

        this.usedVacationDays = usedVacationDays;
    }


    public Integer getVacationDays() {

        return vacationDays;
    }


    public void setVacationDays(Integer vacationDays) {

        this.vacationDays = vacationDays;
    }


    public Integer getYear() {

        return year;
    }


    public void setYear(Integer year) {

        this.year = year;
    }
}
