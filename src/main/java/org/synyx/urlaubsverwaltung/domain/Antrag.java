package org.synyx.urlaubsverwaltung.domain;

import org.joda.time.DateMidnight;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;


/**
 * @author  johannes
 */

@Entity
public class Antrag extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1L;

    // Eine Person kann mehrere Antraege besitzen
    @ManyToOne
    private Person person;

    // Der Chef, der den antrag genehmigt/abgelehnt hat
    @OneToOne
    private Person boss;

    // Grund, warum Chef Antrag abgelehnt hat
    @OneToOne
    private Kommentar reasonToDecline;

    // Abwesenheit insgesamt (also plus Feiertage, Wochenende, etc.)
    private Integer beantragteTageBrutto;

    // Anzahl der Tage netto, die vom Urlaubskonto abgezogen werden
    private Integer beantragteTageNetto;

    // Wenn nachtraeglich hinzugefuegt, werden diese dem Urlaubskonto wieder gutgeschrieben
    private Integer krankheitsTage;

    // Zeitraum von wann bis wann
    private DateMidnight startDate;

    private DateMidnight endDate;

    // z.B. Erholungsurlaub, Sonderurlaub, etc.
    private VacationType vacationType;

    // nur morgens, nur mittags oder ganztägig
    private Length howLong;

    // Grund Pflicht bei Sonderurlaub, unbezahltem Urlaub und Ueberstundenabbummeln
    // bei Erholungsurlaub default = Erholung
    private String reason;

    // Mitarbeiter als Vertreter
    @OneToOne
    private Person vertreter;

    // Anschrift und Telefon waehrend Urlaub
    private String anschrift;

    private String phone;

    // Datum Antragsstellung
    private DateMidnight antragsDate;

    // Status des Antrags (wartend, genehmigt, ...)
    private State status;

    public Person getPerson() {

        return person;
    }


    public Person getBoss() {

        return boss;
    }


    public Kommentar getReasonToDecline() {

        return reasonToDecline;
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


    public Length getHowLong() {

        return howLong;
    }


    public void setHowLong(Length howLong) {

        this.howLong = howLong;
    }


    public State getStatus() {

        return status;
    }


    public void setStatus(State status) {

        this.status = status;
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


    public void setPerson(Person person) {

        this.person = person;
    }


    public void setBoss(Person boss) {

        this.boss = boss;
    }


    public void setReasonToDecline(Kommentar reasonToDecline) {

        this.reasonToDecline = reasonToDecline;
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
}
