package org.synyx.urlaubsverwaltung.companyclosure;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.Instant;
import java.util.Objects;

import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * A company closure ("Betriebsruhe") on a yearly recurring day, e.g. 24.12. The closed portion of the day
 * is stored in {@link CompanyClosureVersionEntity}.
 */
@Entity(name = "company_closure")
public class CompanyClosureEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "company_closure_generator")
    @SequenceGenerator(name = "company_closure_generator", sequenceName = "company_closure_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private int month;

    @Column(name = "day_of_month", nullable = false)
    private int dayOfMonth;

    @Column(name = "message_key", nullable = false)
    private String messageKey;

    /**
     * The last change by a person, {@code null} for rows nobody changed yet, e.g. the defaults or the ones migrated
     * from the settings. Keeps only the last change until the whole history is recorded.
     */
    @Nullable
    @Column(name = "last_modified_at")
    private Instant lastModifiedAt;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "last_modified_by")
    private Person lastModifiedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    @Nullable
    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(@Nullable Instant lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    @Nullable
    public Person getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(@Nullable Person lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public String toString() {
        return "CompanyClosureEntity{" +
            "id=" + id +
            ", month=" + month +
            ", dayOfMonth=" + dayOfMonth +
            ", messageKey='" + messageKey + '\'' +
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
        final CompanyClosureEntity that = (CompanyClosureEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
