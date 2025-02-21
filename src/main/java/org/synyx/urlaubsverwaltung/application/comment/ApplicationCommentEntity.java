package org.synyx.urlaubsverwaltung.application.comment;

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

@Entity(name = "application_comment")
public class ApplicationCommentEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "application_comment_generator")
    @SequenceGenerator(name = "application_comment_generator", sequenceName = "application_comment_id_seq")
    private Long id;

    private Long applicationId;

    @Enumerated(STRING)
    private ApplicationCommentAction action;

    @ManyToOne
    private Person person;

    @Column(nullable = false)
    private final Instant date;

    private String text;

    protected ApplicationCommentEntity() {
        this(null);
    }

    public ApplicationCommentEntity(Clock clock) {
        this(null, clock);
    }

    public ApplicationCommentEntity(Person person, Clock clock) {
        final Clock c = ofNullable(clock).orElse(Clock.systemUTC());
        this.date = Instant.now(c).truncatedTo(DAYS);
        this.person = person;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long application) {
        this.applicationId = application;
    }

    public ApplicationCommentAction getAction() {
        return action;
    }

    public void setAction(ApplicationCommentAction action) {
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
        final ApplicationCommentEntity that = (ApplicationCommentEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
