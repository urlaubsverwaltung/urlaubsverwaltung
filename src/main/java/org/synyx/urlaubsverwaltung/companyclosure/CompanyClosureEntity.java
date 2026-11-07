package org.synyx.urlaubsverwaltung.companyclosure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

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

    @Column(nullable = false)
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CompanyClosureEntity{" +
            "id=" + id +
            ", month=" + month +
            ", dayOfMonth=" + dayOfMonth +
            ", name='" + name + '\'' +
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
