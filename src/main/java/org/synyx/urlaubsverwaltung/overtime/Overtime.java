package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;


/**
 * Represents the overtime of a person for a certain period of time.
 *
 * @since 2.11.0
 */
@Entity
public class Overtime extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 67834589209309L;

    @ManyToOne
    private Person person;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private BigDecimal hours;

    @Column(nullable = false)
    private LocalDate lastModificationDate;

    Overtime() {
        // OK
    }

    public Overtime(Person person, LocalDate startDate, LocalDate endDate, BigDecimal numberOfHours) {

        Assert.notNull(person, "Person must be given.");
        Assert.notNull(startDate, "Start date must be given.");
        Assert.notNull(endDate, "End date must be given.");
        Assert.notNull(numberOfHours, "Number of hours must be given.");

        this.person = person;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hours = numberOfHours;

        this.lastModificationDate = LocalDate.now(UTC);
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

    public BigDecimal getHours() {
        return hours;
    }

    @Override
    public void setId(Integer id) { // NOSONAR - make it public instead of protected
        super.setId(id);
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

    public void setHours(BigDecimal hours) {

        Assert.notNull(hours, "Hours must be given.");

        this.hours = hours;
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
            ", hours=" + hours +
            ", person=" + person +
            '}';
    }
}
