package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;
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
        personBasedataRepository.save(mapToEntity(personBasedata));
    }

    @Override
    public Map<Integer, PersonBasedata> getBasedataByPersonIds(List<Integer> personIds) {

        return personBasedataRepository.findAllByPersonIdIn(personIds).stream()
            .map(PersonBasedataMapper::mapFromEntity)
            .collect(toMap(PersonBasedata::getPersonId, Function.identity()));
    }
}
