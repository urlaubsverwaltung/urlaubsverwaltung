package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataMapper.mapToEntity;

@Service
class PersonBasedataServiceImpl implements PersonBasedataService {

    private final PersonBasedataRepository personBasedataRepository;

    PersonBasedataServiceImpl(PersonBasedataRepository personBasedataRepository) {
        this.personBasedataRepository = personBasedataRepository;
    }

    @Override
    public Optional<PersonBasedata> getBasedataByPersonId(int personId) {
        return personBasedataRepository.findById(personId)
            .map(PersonBasedataMapper::mapFromEntity);
    }

    @Override
    public void update(PersonBasedata personBasedata) {
        final PersonBasedataEntity personBasedataEntity = mapToEntity(personBasedata);
        personBasedataRepository.save(personBasedataEntity);
    }
}
