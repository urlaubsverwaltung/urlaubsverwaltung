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
    private Double vacationDays;

    // verbleibender resturlaub im jahr year
    private Double restVacationDays;

    // in diesem Jahr benutzter normal-Urlaub
    private Double usedVacationDays;

    // in diesem Jahr benutzter resturlaub
    private Double usedRestVacationDays;

    // genommener sonderurlaub im jahr year
    private Double sonderUrlaub;

    // genommener unbezahlter urlaub im jahr year
    private Double unbezahlterUrlaub;

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public Double getRestVacationDays() {

        return restVacationDays;
    }


    public void setRestVacationDays(Double restVacationDays) {

        this.restVacationDays = restVacationDays;
    }


    public Double getSonderUrlaub() {

        return sonderUrlaub;
    }


    public void setSonderUrlaub(Double sonderUrlaub) {

        this.sonderUrlaub = sonderUrlaub;
    }


    public Double getUnbezahlterUrlaub() {

        return unbezahlterUrlaub;
    }


    public void setUnbezahlterUrlaub(Double unbezahlterUrlaub) {

        this.unbezahlterUrlaub = unbezahlterUrlaub;
    }


    public Double getUsedRestVacationDays() {

        return usedRestVacationDays;
    }


    public void setUsedRestVacationDays(Double usedRestVacationDays) {

        this.usedRestVacationDays = usedRestVacationDays;
    }


    public Double getUsedVacationDays() {

        return usedVacationDays;
    }


    public void setUsedVacationDays(Double usedVacationDays) {

        this.usedVacationDays = usedVacationDays;
    }


    public Double getVacationDays() {

        return vacationDays;
    }


    public void setVacationDays(Double vacationDays) {

        this.vacationDays = vacationDays;
    }


    public Integer getYear() {

        return year;
    }


    public void setYear(Integer year) {

        this.year = year;
    }
}
