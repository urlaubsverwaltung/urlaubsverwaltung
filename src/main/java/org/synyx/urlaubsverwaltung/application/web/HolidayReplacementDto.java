package org.synyx.urlaubsverwaltung.application.web;

import org.springframework.beans.BeanUtils;
import org.synyx.urlaubsverwaltung.application.dao.HolidayReplacementEntity;
import org.synyx.urlaubsverwaltung.person.Person;

public class HolidayReplacementDto {

    private Person person;
    private String note;

    public HolidayReplacementDto() {
    }

    public HolidayReplacementDto(Person person) {
        this.person = person;
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

    public static HolidayReplacementDto from(HolidayReplacementEntity holidayReplacementEntity) {
        HolidayReplacementDto holidayReplacementDto = new HolidayReplacementDto();
        BeanUtils.copyProperties(holidayReplacementEntity, holidayReplacementDto);
        return holidayReplacementDto;
    }

    @Override
    public String toString() {
        return "HolidayReplacementDto{" +
            ", person=" + person +
            ", note='" + note + '\'' +
            '}';
    }
}
