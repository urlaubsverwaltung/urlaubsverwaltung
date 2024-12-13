package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.stereotype.Service;

@Service
public class PersonCalendarImportService {

    private final PersonCalendarRepository personCalendarRepository;

    PersonCalendarImportService(PersonCalendarRepository personCalendarRepository) {
        this.personCalendarRepository = personCalendarRepository;
    }

    public void deleteAll() {
        personCalendarRepository.deleteAll();
    }

    public void importPersonCalendar(PersonCalendar personCalendar) {
        personCalendarRepository.save(personCalendar);
    }
}
