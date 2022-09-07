package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
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
    public Map<PersonId, PersonBasedata> getBasedataByPersonId(List<Integer> personIds) {
        return personBasedataRepository.findAllByPersonIdIn(personIds).stream()
            .map(PersonBasedataMapper::mapFromEntity)
            .collect(toMap((o) -> new PersonId(o.getPersonId()), identity()));
    }

    @Override
    public void update(PersonBasedata personBasedata) {
        personBasedataRepository.save(mapToEntity(personBasedata));
    }
}
