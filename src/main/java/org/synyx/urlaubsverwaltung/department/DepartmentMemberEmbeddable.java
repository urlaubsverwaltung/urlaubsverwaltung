package org.synyx.urlaubsverwaltung.department;

import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.Instant;
import java.util.Objects;

@Embeddable
class DepartmentMemberEmbeddable {

    @ManyToOne
    @JoinColumn(name = "members_id", nullable = false)
    private Person person;

    @Column(name = "accession_date")
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
