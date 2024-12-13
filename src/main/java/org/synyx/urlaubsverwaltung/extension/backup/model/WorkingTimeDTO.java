package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeEntity;

import java.time.LocalDate;

public record WorkingTimeDTO(DayLengthDTO monday, DayLengthDTO tuesday, DayLengthDTO wednesday, DayLengthDTO thursday,
                             DayLengthDTO friday, DayLengthDTO saturday, DayLengthDTO sunday, LocalDate validFrom,
                             FederalStateDTO federalState, boolean defaultFederalState) {

    public WorkingTimeEntity toWorkingTimeEntity(Person person) {
        WorkingTimeEntity entity = new WorkingTimeEntity();
        entity.setPerson(person);
        entity.setMonday(this.monday.toDayLength());
        entity.setTuesday(this.tuesday.toDayLength());
        entity.setWednesday(this.wednesday.toDayLength());
        entity.setThursday(this.thursday.toDayLength());
        entity.setFriday(this.friday.toDayLength());
        entity.setSaturday(this.saturday.toDayLength());
        entity.setSunday(this.sunday.toDayLength());
        entity.setValidFrom(this.validFrom);

        if (!this.defaultFederalState) {
            entity.setFederalStateOverride(this.federalState.toFederalState());
        }

        return entity;
    }
}
