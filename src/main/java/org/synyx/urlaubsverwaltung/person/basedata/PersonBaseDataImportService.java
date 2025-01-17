package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.stereotype.Service;

@Service
public class PersonBaseDataImportService {

    private final PersonBasedataRepository personBasedataRepository;


    PersonBaseDataImportService(PersonBasedataRepository personBasedataRepository) {
        this.personBasedataRepository = personBasedataRepository;
    }

    public void deleteAll() {
        personBasedataRepository.deleteAll();
    }

    public void importPersonBaseData(PersonBasedataEntity personBasedataEntity) {
        personBasedataRepository.save(personBasedataEntity);
    }
}
