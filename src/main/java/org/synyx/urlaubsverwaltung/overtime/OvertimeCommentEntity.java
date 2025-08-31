package org.synyx.urlaubsverwaltung.overtime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.ofNullable;

/**
 * Recorded comment after executed an overtime action, e.g. create a new overtime record.
 *
 * @since 2.11.0
 */
@Entity(name = "overtime_comment")
public class OvertimeCommentEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "overtime_comment_generator")
    @SequenceGenerator(name = "overtime_comment_generator", sequenceName = "overtime_comment_id_seq")
    private Long id;

    @ManyToOne
    private OvertimeEntity overtime;

    @Column(nullable = false)
    @Enumerated(STRING)
    private OvertimeCommentAction action;

    @ManyToOne
    private Person person;

    @Column(nullable = false)
    private Instant date;

    private String text;

    protected OvertimeCommentEntity() {
        // needed for hibernate
    }

    public OvertimeCommentEntity(Clock clock) {
        final Clock c = ofNullable(clock).orElse(Clock.systemUTC());
        this.date = Instant.now(c).truncatedTo(DAYS);
    }

    public OvertimeCommentEntity(Person author, OvertimeEntity overtime, OvertimeCommentAction action, Clock clock) {
        final Clock c = ofNullable(clock).orElse(Clock.systemUTC());
        this.date = Instant.now(c).truncatedTo(DAYS);
        setPerson(author);

        this.overtime = overtime;
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OvertimeEntity getOvertime() {
        return overtime;
    }

    public void setOvertime(OvertimeEntity overtime) {
        this.overtime = overtime;
    }

    public OvertimeCommentAction getAction() {
        return action;
    }

    public void setAction(OvertimeCommentAction action) {
        this.action = action;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Instant getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OvertimeCommentEntity that = (OvertimeCommentEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OvertimeComment{" +
            "id=" + id +
            ", overtime=" + overtime +
            ", action=" + action +
            ", person=" + person +
            ", date=" + date +
            ", text='" + text + '\'' +
            '}';
    }
}
