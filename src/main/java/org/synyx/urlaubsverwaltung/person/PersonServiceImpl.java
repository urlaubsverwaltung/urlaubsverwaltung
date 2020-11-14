package org.synyx.urlaubsverwaltung.person;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;


/**
 * Implementation for {@link PersonService}.
 */
@Service("personService")
class PersonServiceImpl implements PersonService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonRepository personRepository;
    private final AccountInteractionService accountInteractionService;
    private final WorkingTimeService workingTimeService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    PersonServiceImpl(PersonRepository personRepository, AccountInteractionService accountInteractionService,
                      WorkingTimeService workingTimeService, ApplicationEventPublisher applicationEventPublisher) {

        this.personRepository = personRepository;
        this.accountInteractionService = accountInteractionService;
        this.workingTimeService = workingTimeService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Person create(String username, String lastName, String firstName, String email,
                         List<MailNotification> notifications, List<Role> permissions) {

        final Person person = new Person(username, lastName, firstName, email);
        person.setNotifications(notifications);
        person.setPermissions(permissions);

        return create(person);
    }

    @Override
    public Person create(Person person) {

        final Person createdPerson = save(person);
        LOG.info("Created person: {}", person);

        accountInteractionService.createDefaultAccount(person);
        workingTimeService.createDefaultWorkingTime(person);

        return createdPerson;
    }

    @Override
    public Person update(Integer id, String username, String lastName, String firstName, String email,
                         List<MailNotification> notifications, List<Role> permissions) {

        final Person person = getPersonByID(id)
            .orElseThrow(() -> new IllegalArgumentException("Can not find a person for ID = " + id));

        person.setUsername(username);
        person.setLastName(lastName);
        person.setFirstName(firstName);
        person.setEmail(email);
        person.setNotifications(notifications);
        person.setPermissions(permissions);

        LOG.info("Update person: {}", person);

        return save(person);
    }

    @Override
    public Person update(Person person) {

        if (person.getId() == null) {
            throw new IllegalArgumentException("Can not update a person that is not persisted yet");
        }

        LOG.info("Updated person: {}", person);

        return save(person);
    }

    @Override
    public Person save(Person person) {

        final Person persistedPerson = personRepository.save(person);

        final boolean isInactive = persistedPerson.getPermissions().contains(INACTIVE);
        if (isInactive) {
            applicationEventPublisher.publishEvent(new PersonDisabledEvent(this, persistedPerson.getId()));
        }

        return persistedPerson;
    }

    @Override
    public Optional<Person> getPersonByID(Integer id) {
        return personRepository.findById(id);
    }

    @Override
    public Optional<Person> getPersonByUsername(String username) {
        return Optional.ofNullable(personRepository.findByUsername(username));
    }

    @Override
    public List<Person> getActivePersons() {
        return personRepository.findAll()
            .stream()
            .filter(person -> !person.hasRole(INACTIVE))
            .sorted(personComparator())
            .collect(toList());
    }

    @Override
    public List<Person> getInactivePersons() {
        return personRepository.findAll()
            .stream()
            .filter(person -> person.hasRole(INACTIVE))
            .sorted(personComparator())
            .collect(toList());
    }

    @Override
    public List<Person> getActivePersonsByRole(final Role role) {
        return getActivePersons().stream()
            .filter(person -> person.hasRole(role))
            .collect(toList());
    }

    @Override
    public List<Person> getPersonsWithNotificationType(final MailNotification notification) {
        return getActivePersons().stream()
            .filter(person -> person.hasNotificationType(notification))
            .collect(toList());
    }

    @Override
    public Person getSignedInUser() {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authentication found in context.");
        }

        final String username = authentication.getName();
        final Optional<Person> person = getPersonByUsername(username);
        if (person.isEmpty()) {
            throw new IllegalStateException("Can not get the person for the signed in user with username = " + username);
        }

        return person.get();
    }

    /**
     * Adds {@link Role#OFFICE} to the roles of the given person if no
     * other active user with a office role is defined.
     *
     * @param person that maybe gets the role {@link Role#OFFICE}
     * @return saved {@link Person} with {@link Role#OFFICE} rights
     * if no other active person with {@link Role#OFFICE} is available.
     */
    @Override
    public Person appointAsOfficeUserIfNoOfficeUserPresent(Person person) {

        boolean activeOfficeUserAvailable = !getActivePersonsByRole(OFFICE).isEmpty();
        if (activeOfficeUserAvailable) {
            return person;
        }

        final List<Role> permissions = new ArrayList<>(person.getPermissions());
        permissions.add(OFFICE);
        person.setPermissions(permissions);

        final Person savedPerson = save(person);

        LOG.info("Add 'OFFICE' role to person: {}", person);

        return savedPerson;
    }

    private Comparator<Person> personComparator() {
        return Comparator.comparing(p -> p.getNiceName().toLowerCase());
    }
}
