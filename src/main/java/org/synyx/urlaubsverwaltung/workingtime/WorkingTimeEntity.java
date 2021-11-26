package org.synyx.urlaubsverwaltung.workingtime;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.time.LocalDate;
import java.util.Objects;

import static javax.persistence.EnumType.STRING;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;


/**
 * Entity representing the working time of a person.
 */
@Entity(name = "working_time")
class WorkingTimeEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GenericGenerator(
        name = "working_time_id_seq",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "working_time_id_seq"),
            @Parameter(name = "initial_value", value = "1"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "working_time_id_seq")
    private Integer id;

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

    Integer getId() {
        return id;
    }

    void setId(Integer id) {
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
