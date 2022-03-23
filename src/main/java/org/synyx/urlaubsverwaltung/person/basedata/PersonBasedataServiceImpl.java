package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataMapper.mapFromEntity;
import static org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataMapper.mapToEntity;

@Service
public class PersonBasedataServiceImpl implements PersonBasedataService {

    private final PersonBasedataRepository personBasedataRepository;

    public PersonBasedataServiceImpl(PersonBasedataRepository personBasedataRepository) {
        this.personBasedataRepository = personBasedataRepository;
    }

    @Override
    public Optional<PersonBasedata> getBasedataByPersonId(int personId) {

        final Optional<PersonBasedataEntity> personBasedataEntity = personBasedataRepository.findById(personId);

        if(personBasedataEntity.isPresent()) {
            PersonBasedata personBasedata = mapFromEntity(personBasedataEntity.get());
            return Optional.of(personBasedata);
        }

        return Optional.empty();
    }

    @Override
    public void update(PersonBasedata personBasedata) {
        PersonBasedataEntity personBasedataEntity = mapToEntity(personBasedata);
        personBasedataRepository.save(personBasedataEntity);
    }
}
