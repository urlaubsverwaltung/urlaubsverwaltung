package org.synyx.urlaubsverwaltung.workingtime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;


/**
 * Entity representing the working time of a person.
 */
@Entity(name = "working_time")
class WorkingTimeEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "working_time_generator")
    @SequenceGenerator(name = "working_time_generator", sequenceName = "working_time_id_seq")
    private Long id;

    @OneToOne
    private Person person;

    @Enumerated(STRING)
    private DayLength monday = ZERO;

    @Enumerated(STRING)
    private DayLength tuesday = ZERO;

    @Enumerated(STRING)
    private DayLength wednesday = ZERO;

    @Enumerated(STRING)
    private DayLength thursday = ZERO;

    @Enumerated(STRING)
    private DayLength friday = ZERO;

    @Enumerated(STRING)
    private DayLength saturday = ZERO;

    @Enumerated(STRING)
    private DayLength sunday = ZERO;

    private LocalDate validFrom;

    /**
     * If set, override the system-wide FederalState setting for this person.
     */
    @Enumerated(STRING)
    private FederalState federalStateOverride;

    Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    Person getPerson() {
        return person;
    }

    void setPerson(Person person) {
        this.person = person;
    }

    LocalDate getValidFrom() {
        return this.validFrom;
    }

    void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public DayLength getMonday() {
        return monday;
    }

    public void setMonday(DayLength monday) {
        this.monday = monday;
    }

    public DayLength getTuesday() {
        return tuesday;
    }

    public void setTuesday(DayLength tuesday) {
        this.tuesday = tuesday;
    }

    public DayLength getWednesday() {
        return wednesday;
    }

    public void setWednesday(DayLength wednesday) {
        this.wednesday = wednesday;
    }

    public DayLength getThursday() {
        return thursday;
    }

    public void setThursday(DayLength thursday) {
        this.thursday = thursday;
    }

    public DayLength getFriday() {
        return friday;
    }

    public void setFriday(DayLength friday) {
        this.friday = friday;
    }

    public DayLength getSaturday() {
        return saturday;
    }

    public void setSaturday(DayLength saturday) {
        this.saturday = saturday;
    }

    public DayLength getSunday() {
        return sunday;
    }

    public void setSunday(DayLength sunday) {
        this.sunday = sunday;
    }

    FederalState getFederalStateOverride() {
        return federalStateOverride;
    }

    void setFederalStateOverride(FederalState federalState) {
        this.federalStateOverride = federalState;
    }

    @Override
    public String toString() {
        return "WorkingTimeEntity{" +
            "person=" + person +
            ", monday=" + monday +
            ", tuesday=" + tuesday +
            ", wednesday=" + wednesday +
            ", thursday=" + thursday +
            ", friday=" + friday +
            ", saturday=" + saturday +
            ", sunday=" + sunday +
            ", validFrom=" + validFrom +
            ", federalStateOverride=" + federalStateOverride +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final WorkingTimeEntity that = (WorkingTimeEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
