package org.synyx.urlaubsverwaltung.person.masterdata;

import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.synyx.urlaubsverwaltung.person.masterdata.PersonBasedataMapper.mapFromEntity;
import static org.synyx.urlaubsverwaltung.person.masterdata.PersonBasedataMapper.mapToEntity;

@Service
public class PersonBasedataService {

    private final PersonBasedataRepository personBasedataRepository;

    public PersonBasedataService(PersonBasedataRepository personBasedataRepository) {
        this.personBasedataRepository = personBasedataRepository;
    }

    public Optional<PersonBasedata> getBasedataByPersonId(int personId) {

        final Optional<PersonBasedataEntity> personBasedataEntity = personBasedataRepository.findById(personId);

        if(personBasedataEntity.isPresent()) {
            PersonBasedata personBasedata = mapFromEntity(personBasedataEntity.get());
            return Optional.of(personBasedata);
        }

        return Optional.empty();
    }

    public void update(PersonBasedata personBasedata) {
        PersonBasedataEntity personBasedataEntity = mapToEntity(personBasedata);
        personBasedataRepository.save(personBasedataEntity);
    }
}
