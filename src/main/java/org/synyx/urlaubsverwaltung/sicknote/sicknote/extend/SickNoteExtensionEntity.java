package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity(name = "sick_note_extension")
public class SickNoteExtensionEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "sick_note_extension_generator")
    @SequenceGenerator(name = "sick_note_extension_generator", sequenceName = "sick_note_extension_id_seq")
    private Long id;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "sick_note_id")
    private Long sickNoteId;

    @Column(name = "new_end_date")
    private LocalDate newEndDate;

    @Column(name = "is_aub")
    private boolean isAub;

    @Enumerated(STRING)
    private SickNoteExtensionStatus status;

    public Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    public Long getSickNoteId() {
        return sickNoteId;
    }

    public void setSickNoteId(Long extensionOfSickNoteId) {
        this.sickNoteId = extensionOfSickNoteId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getNewEndDate() {
        return newEndDate;
    }

    public void setNewEndDate(LocalDate newEndDate) {
        this.newEndDate = newEndDate;
    }

    public boolean isAub() {
        return isAub;
    }

    public void setAub(boolean aub) {
        isAub = aub;
    }

    public SickNoteExtensionStatus getStatus() {
        return status;
    }

    public void setStatus(SickNoteExtensionStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SickNoteExtensionEntity that = (SickNoteExtensionEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SickNoteExtensionEntity{" +
            "id=" + id +
            ", sickNoteId=" + sickNoteId +
            ", createdAt=" + createdAt +
            ", newEndDate=" + newEndDate +
            ", isAub=" + isAub +
            ", status=" + status +
            '}';
    }
}
