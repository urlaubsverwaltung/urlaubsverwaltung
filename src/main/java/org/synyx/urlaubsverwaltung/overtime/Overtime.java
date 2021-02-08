package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;


/**
 * Represents the overtime of a person for a certain period of time.
 *
 * @since 2.11.0
 */
@Entity
public class Overtime {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Person person;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @Convert(converter = DurationConverter.class)
    private Duration duration;

    @Column(nullable = false)
    private LocalDate lastModificationDate;

    protected Overtime() {
        // OK
    }

    public Overtime(Person person, LocalDate startDate, LocalDate endDate, Duration duration) {

        Assert.notNull(person, "Person must be given.");
        Assert.notNull(startDate, "Start date must be given.");
        Assert.notNull(endDate, "End date must be given.");
        Assert.notNull(duration, "Duration of must be given.");

        this.person = person;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;

        this.lastModificationDate = LocalDate.now(UTC);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public LocalDate getStartDate() {
        if (startDate == null) {
            throw new IllegalStateException("Missing start date!");
        }

        return startDate;
    }

    public LocalDate getEndDate() {
        if (endDate == null) {
            throw new IllegalStateException("Missing end date!");
        }

        return endDate;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setPerson(Person person) {

        Assert.notNull(person, "Person must be given.");

        this.person = person;
    }

    public void setStartDate(LocalDate startDate) {

        Assert.notNull(startDate, "Start date must be given.");

        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {

        Assert.notNull(endDate, "End date must be given.");

        this.endDate = endDate;
    }

    public void setDuration(Duration duration) {

        Assert.notNull(duration, "Duration of overtime must be given.");

        this.duration = duration;
    }

    public LocalDate getLastModificationDate() {
        if (lastModificationDate == null) {
            throw new IllegalStateException("Missing last modification date!");
        }

        return this.lastModificationDate;
    }

    /**
     * Should be called whenever an overtime entity is updated.
     */
    public void onUpdate() {
        this.lastModificationDate = LocalDate.now(UTC);
    }

    @Override
    public String toString() {
        return "Overtime{" +
            "id=" + getId() +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", duration=" + duration +
            ", person=" + person +
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
        final Overtime that = (Overtime) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
