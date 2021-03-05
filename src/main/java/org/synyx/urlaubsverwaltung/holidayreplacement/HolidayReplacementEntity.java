package org.synyx.urlaubsverwaltung.holidayreplacement;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity(name="holiday_replacement")
public class HolidayReplacementEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @NotNull
    @OneToOne
    private Person person;

    private String note;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "HolidayReplacement{" +
            ", person=" + person +
            ", note='" + note + '\'' +
            '}';
    }
}
