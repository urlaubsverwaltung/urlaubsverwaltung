package org.synyx.urlaubsverwaltung.domain;

import org.joda.time.DateMidnight;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


/**
 * @author  johannes
 */

@Entity
public class Antrag extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1L;

    // Eine Person kann mehrere Antraege besitzen
    @ManyToOne
    private Person person;
    
    //Der Chef, der den antrag genehmigt, abgelehnt hat
    private Person boss;

    // Abwesenheit insgesamt (also plus Feiertage, Wochenende, etc.)
    private Integer beantragteTageBrutto;

    // Anzahl der Tage netto, die vom Urlaubskonto abgezogen werden
    private Integer beantragteTageNetto;

    // Wenn nachtraeglich hinzugefuegt, werden diese dem Urlaubskonto gutgeschrieben
    private Integer krankheitsTage;

    // Zeitraum von wann bis wann
    private DateMidnight startDate;

    private DateMidnight endDate;

    // z.B. Erholungsurlaub
    private VacationType vacationType;

    // Grund Pflicht bei Sonderurlaub, unbezahltem Urlaub und Ueberstundenabbummeln
    // bei Erholungsurlaub default = Erholung
    private String reason;

    // Mitarbeiter als Vertreter
    private Person vertreter;

    private String anschrift;

    private String phone;

    private DateMidnight antragsDate;

    private State state;

    public Person getPerson() {

        return person;
    }


    public Integer getBeantragteTageBrutto() {

        return beantragteTageBrutto;
    }


    public Integer getBeantragteTageNetto() {

        return beantragteTageNetto;
    }


    public Integer getKrankheitsTage() {

        return krankheitsTage;
    }


    public DateMidnight getStartDate() {

        return startDate;
    }


    public DateMidnight getEndDate() {

        return endDate;
    }


    public VacationType getVacationType() {

        return vacationType;
    }


    public String getReason() {

        return reason;
    }


    public Person getVertreter() {

        return vertreter;
    }


    public String getAnschrift() {

        return anschrift;
    }


    public String getPhone() {

        return phone;
    }


    public DateMidnight getAntragsDate() {

        return antragsDate;
    }


    public State getState() {

        return state;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public void setBeantragteTageBrutto(Integer beantragteTageBrutto) {

        this.beantragteTageBrutto = beantragteTageBrutto;
    }


    public void setBeantragteTageNetto(Integer beantragteTageNetto) {

        this.beantragteTageNetto = beantragteTageNetto;
    }


    public void setKrankheitsTage(Integer krankheitsTage) {

        this.krankheitsTage = krankheitsTage;
    }


    public void setStartDate(DateMidnight startDate) {

        this.startDate = startDate;
    }


    public void setEndDate(DateMidnight endDate) {

        this.endDate = endDate;
    }


    public void setVacationType(VacationType vacationType) {

        this.vacationType = vacationType;
    }


    public void setReason(String reason) {

        this.reason = reason;
    }


    public void setVertreter(Person vertreter) {

        this.vertreter = vertreter;
    }


    public void setAnschrift(String anschrift) {

        this.anschrift = anschrift;
    }


    public void setPhone(String phone) {

        this.phone = phone;
    }


    public void setAntragsDate(DateMidnight antragsDate) {

        this.antragsDate = antragsDate;
    }


    public void setState(State state) {

        this.state = state;
    }
}
