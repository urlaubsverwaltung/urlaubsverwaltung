package org.synyx.urlaubsverwaltung.department;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;
import java.util.Objects;

@Embeddable
public class DepartmentMemberEmbeddable {

    @ManyToOne
    @JoinColumn(name = "members_id", nullable = false)
    private Person person;

    @Column(name = "accession_date", nullable = false)
    private Instant accessionDate;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Instant getAccessionDate() {
        return accessionDate;
    }

    public void setAccessionDate(Instant accessionDate) {
        this.accessionDate = accessionDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepartmentMemberEmbeddable that = (DepartmentMemberEmbeddable) o;
        return Objects.equals(getPerson(), that.getPerson());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPerson());
    }
}
