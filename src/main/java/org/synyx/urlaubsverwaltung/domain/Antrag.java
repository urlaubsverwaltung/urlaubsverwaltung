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

    // Anzahl der Tage netto, die vom Urlaubskonto abgezogen werden
    private Double beantragteTageNetto;

    // Wenn nachtraeglich hinzugefuegt, werden diese dem Urlaubskonto wieder gutgeschrieben
    private Double krankheitsTage;

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

    // Signatur des Antragstellers
    private byte[] signedDataPerson;

    // Signatur des Bosses
    private byte[] signedDataBoss;

    public String getAnschrift() {

        return anschrift;
    }


    public void setAnschrift(String anschrift) {

        this.anschrift = anschrift;
    }


    public DateMidnight getAntragsDate() {

        return antragsDate;
    }


    public void setAntragsDate(DateMidnight antragsDate) {

        this.antragsDate = antragsDate;
    }


    public Double getBeantragteTageNetto() {

        return beantragteTageNetto;
    }


    public void setBeantragteTageNetto(Double beantragteTageNetto) {

        this.beantragteTageNetto = beantragteTageNetto;
    }


    public Person getBoss() {

        return boss;
    }


    public void setBoss(Person boss) {

        this.boss = boss;
    }


    public DateMidnight getEndDate() {

        return endDate;
    }


    public void setEndDate(DateMidnight endDate) {

        this.endDate = endDate;
    }


    public Length getHowLong() {

        return howLong;
    }


    public void setHowLong(Length howLong) {

        this.howLong = howLong;
    }


    public Double getKrankheitsTage() {

        return krankheitsTage;
    }


    public void setKrankheitsTage(Double krankheitsTage) {

        this.krankheitsTage = krankheitsTage;
    }


    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public String getPhone() {

        return phone;
    }


    public void setPhone(String phone) {

        this.phone = phone;
    }


    public String getReason() {

        return reason;
    }


    public void setReason(String reason) {

        this.reason = reason;
    }


    public Kommentar getReasonToDecline() {

        return reasonToDecline;
    }


    public void setReasonToDecline(Kommentar reasonToDecline) {

        this.reasonToDecline = reasonToDecline;
    }


    public byte[] getSignedDataBoss() {

        return signedDataBoss;
    }


    public void setSignedDataBoss(byte[] signedDataBoss) {

        this.signedDataBoss = signedDataBoss;
    }


    public byte[] getSignedDataPerson() {

        return signedDataPerson;
    }


    public void setSignedDataPerson(byte[] signedDataPerson) {

        this.signedDataPerson = signedDataPerson;
    }


    public DateMidnight getStartDate() {

        return startDate;
    }


    public void setStartDate(DateMidnight startDate) {

        this.startDate = startDate;
    }


    public State getStatus() {

        return status;
    }


    public void setStatus(State status) {

        this.status = status;
    }


    public VacationType getVacationType() {

        return vacationType;
    }


    public void setVacationType(VacationType vacationType) {

        this.vacationType = vacationType;
    }


    public Person getVertreter() {

        return vertreter;
    }


    public void setVertreter(Person vertreter) {

        this.vertreter = vertreter;
    }
}
