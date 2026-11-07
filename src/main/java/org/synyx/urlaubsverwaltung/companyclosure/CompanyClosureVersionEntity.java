package org.synyx.urlaubsverwaltung.companyclosure;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.LocalDate;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * The closed portion of a company closure, valid from the given date on (or for all years if {@code validFrom}
 * is {@code null}).
 */
@Entity(name = "company_closure_version")
public class CompanyClosureVersionEntity extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "company_closure_version_generator")
    @SequenceGenerator(name = "company_closure_version_generator", sequenceName = "company_closure_version_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "company_closure_id", nullable = false)
    private Long companyClosureId;

    @Enumerated(STRING)
    @Column(name = "day_length", nullable = false)
    private DayLength dayLength;

    @Nullable
    @Column(name = "valid_from")
    private LocalDate validFrom;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyClosureId() {
        return companyClosureId;
    }

    public void setCompanyClosureId(Long companyClosureId) {
        this.companyClosureId = companyClosureId;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public void setDayLength(DayLength dayLength) {
        this.dayLength = dayLength;
    }

    @Nullable
    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(@Nullable LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public String toString() {
        return "CompanyClosureVersionEntity{" +
            "id=" + id +
            ", companyClosureId=" + companyClosureId +
            ", dayLength=" + dayLength +
            ", validFrom=" + validFrom +
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
        final CompanyClosureVersionEntity that = (CompanyClosureVersionEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
