package org.synyx.urlaubsverwaltung.person.extension;

import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonServiceExtension;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.ConditionalOnExtensionsEnabled;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PageStreamSupport;

import java.util.Optional;
import java.util.stream.Stream;

import static org.synyx.urlaubsverwaltung.person.extension.PersonDTOMapper.toPerson;
import static org.synyx.urlaubsverwaltung.person.extension.PersonDTOMapper.toPersonDTO;
import static org.synyx.urlaubsverwaltung.person.extension.PersonDTOMapper.toPersonUpdate;

@ConditionalOnExtensionsEnabled
@Service
public class PersonServiceExtensionImpl implements PersonServiceExtension {

    private final PersonService personService;

    public PersonServiceExtensionImpl(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public PersonDTO create(PersonDTO person) {
        final Person created = personService.create(
            person.username(),
            person.firstName(),
            person.lastName(),
            person.email()
        );
        return toPersonDTO(created);
    }

    @Override
    public PersonDTO update(PersonDTO person) {
        final Person update = personService.update(new PersonId(person.id()), toPersonUpdate(person));
        return toPersonDTO(update);
    }

    @Override
    public void delete(PersonDTO person, Long signedInUserId) {
        personService.delete(new PersonId(person.id()), new PersonId(signedInUserId));
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
        return PageStreamSupport.stream(pageable -> personService.getActivePersons(PersonPageRequest.of(pageable)))
            .map(PersonDTOMapper::toPersonDTO);
    }

    @Override
    public Stream<PersonDTO> getInactivePersons() {
        return PageStreamSupport.stream(pageable -> personService.getInactivePersons(PersonPageRequest.of(pageable)))
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
