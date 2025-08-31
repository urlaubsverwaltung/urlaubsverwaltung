package org.synyx.urlaubsverwaltung.overtime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.DurationConverter;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

import static jakarta.persistence.GenerationType.SEQUENCE;
import static java.time.ZoneOffset.UTC;

/**
 * Represents the overtime of a person for a certain period of time.
 *
 * @since 2.11.0
 */
@Entity(name = "overtime")
public class OvertimeEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "overtime_generator")
    @SequenceGenerator(name = "overtime_generator", sequenceName = "overtime_id_seq")
    private Long id;

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
    private boolean external = false;

    @Column(nullable = false)
    private LocalDate lastModificationDate;

    protected OvertimeEntity() {
        // OK
    }

    public OvertimeEntity(Person person, LocalDate startDate, LocalDate endDate, Duration duration) {
        this(person, startDate, endDate, duration, false, LocalDate.now(UTC));
    }

    public OvertimeEntity(Person person, LocalDate startDate, LocalDate endDate, Duration duration, boolean external) {
        this(person, startDate, endDate, duration, external, LocalDate.now(UTC));
    }


    public OvertimeEntity(Person person, LocalDate startDate, LocalDate endDate, Duration duration, boolean external, LocalDate lastModificationDate) {
        this.person = person;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.external = external;
        this.lastModificationDate = lastModificationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        this.person = person;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public void setLastModificationDate(LocalDate lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
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
        return "OvertimeEntity{" +
                "id=" + getId() +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", duration=" + duration +
                ", external=" + external +
                ", person=" + person +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OvertimeEntity that = (OvertimeEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
