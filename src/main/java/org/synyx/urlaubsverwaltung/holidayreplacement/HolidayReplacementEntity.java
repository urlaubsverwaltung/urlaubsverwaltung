package org.synyx.urlaubsverwaltung.holidayreplacement;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Embeddable
public class HolidayReplacementEntity {

    @NotNull
    @OneToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    private String note;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public static HolidayReplacementEntity from(HolidayReplacementDto holidayReplacementDto) {
        HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        BeanUtils.copyProperties(holidayReplacementDto, holidayReplacementEntity);
        return holidayReplacementEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HolidayReplacementEntity that = (HolidayReplacementEntity) o;
        return Objects.equals(getPerson(), that.getPerson()) && Objects.equals(getNote(), that.getNote());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPerson(), getNote());
    }

    @Override
    public String toString() {
        return "HolidayReplacement{" +
            ", person=" + person +
            ", note='" + note + '\'' +
            '}';
    }
}
