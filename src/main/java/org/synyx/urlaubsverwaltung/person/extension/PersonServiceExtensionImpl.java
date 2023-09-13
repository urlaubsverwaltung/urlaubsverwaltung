package org.synyx.urlaubsverwaltung.person.extension;

import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonServiceExtension;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.ExtensionConfiguration;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PageStreamSupport;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;

import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.extension.PersonDTOMapper.toPerson;
import static org.synyx.urlaubsverwaltung.person.extension.PersonDTOMapper.toPersonDTO;

@ConditionalOnBean(ExtensionConfiguration.class)
@Service
public class PersonServiceExtensionImpl implements PersonServiceExtension {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;

    public PersonServiceExtensionImpl(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public PersonDTO create(PersonDTO person) {
        final Person created = personService.create(
            person.getUsername(),
            person.getFirstName(),
            person.getLastName(),
            person.getEmail()
        );
        return toPersonDTO(created);
    }

    @Override
    public PersonDTO update(PersonDTO person) {
        final Person update = personService.update(toPerson(person));
        return toPersonDTO(update);
    }

    @Override
    public void delete(PersonDTO person, Long signedInUserId) {
        personService.getPersonByID(signedInUserId)
            .ifPresentOrElse(
                signedInUser -> personService.delete(toPerson(person), signedInUser),
                () -> LOG.warn("trying to delete person={}, but the person={} who wants to delete the given person doesn't exists - skipped delete!", person.getId(), signedInUserId)
            );
    }

    @Override
    public Optional<PersonDTO> getPersonById(Long id) {
        return personService.getPersonByID(id)
            .map(PersonDTOMapper::toPersonDTO);
    }

    @Override
    public Optional<PersonDTO> getPersonByUsername(String username) {
        return personService.getPersonByUsername(username)
            .map(PersonDTOMapper::toPersonDTO);
    }

    @Override
    public Optional<PersonDTO> getPersonByMailAddress(String mailAddress) {
        return personService.getPersonByMailAddress(mailAddress)
            .map(PersonDTOMapper::toPersonDTO);
    }

    @Override
    public Stream<PersonDTO> getActivePersons() {
        return PageStreamSupport.stream(pageable -> personService.getActivePersons(new PageableSearchQuery(pageable)))
            .map(PersonDTOMapper::toPersonDTO);
    }

    @Override
    public Stream<PersonDTO> getInactivePersons() {
        return PageStreamSupport.stream(pageable -> personService.getInactivePersons(new PageableSearchQuery(pageable)))
            .map(PersonDTOMapper::toPersonDTO);
    }

    @Override
    public PersonDTO getSignedInUser() {
        return toPersonDTO(personService.getSignedInUser());
    }

    @Override
    public PersonDTO appointAsInitialUserIfNoInitialUserPresent(PersonDTO person) {
        final Person updated = personService.appointAsOfficeUserIfNoOfficeUserPresent(toPerson(person));
        return toPersonDTO(updated);
    }

}
