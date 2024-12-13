package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.application.application.HolidayReplacementEntity;
import org.synyx.urlaubsverwaltung.person.Person;

/**
 * @param externalId The external ID of the holiday replacement
 * @param note       The note for the holiday replacement
 */
public record HolidayReplacementDTO(String externalId, String note) {

    public HolidayReplacementEntity toHolidayReplacementEntity(Person person) {
        final HolidayReplacementEntity entity = new HolidayReplacementEntity();
        entity.setPerson(person);
        entity.setNote(this.note);
        return entity;
    }
}
