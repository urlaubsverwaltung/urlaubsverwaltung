package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
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
    public Optional<PersonBasedata> getBasedataByPersonId(long personId) {
        return personBasedataRepository.findById(personId)
            .map(PersonBasedataMapper::mapFromEntity);
    }

    @Override
    public Map<PersonId, PersonBasedata> getBasedataByPersonId(List<Long> personIds) {
        return personBasedataRepository.findAllByPersonIdIsIn(personIds).stream()
            .map(PersonBasedataMapper::mapFromEntity)
            .collect(toMap(PersonBasedata::personId, identity()));
    }

    @Override
    public void update(PersonBasedata personBasedata) {
        personBasedataRepository.save(mapToEntity(personBasedata));
    }

    /**
     * Deletes {@link PersonBasedata} in the database of person id.
     *
     * @param event includes the id of the person to be deleted
     */
    @EventListener
    void delete(PersonDeletedEvent event) {
        personBasedataRepository.deleteByPerson(event.person());
    }
}
