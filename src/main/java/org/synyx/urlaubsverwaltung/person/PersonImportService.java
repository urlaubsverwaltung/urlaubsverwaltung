package org.synyx.urlaubsverwaltung.person;

import org.springframework.stereotype.Service;

@Service
public class PersonImportService {

    private final PersonRepository personRepository;

    PersonImportService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void deleteAll() {
        personRepository.deleteAll();
    }

    public Person importPerson(Person person) {
        return personRepository.save(person);
    }
}
