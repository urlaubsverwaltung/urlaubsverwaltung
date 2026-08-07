package org.synyx.urlaubsverwaltung.person;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Service
class PersonServiceImpl implements PersonService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonRepository personRepository;
    private final AccountInteractionService accountInteractionService;
    private final WorkingTimeWriteService workingTimeWriteService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    @Autowired
    PersonServiceImpl(
        PersonRepository personRepository, AccountInteractionService accountInteractionService,
        WorkingTimeWriteService workingTimeWriteService, ApplicationEventPublisher applicationEventPublisher,
        Clock clock
    ) {
        this.personRepository = personRepository;
        this.accountInteractionService = accountInteractionService;
        this.workingTimeWriteService = workingTimeWriteService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Person create(String username, String firstName, String lastName, String email) {

        final List<MailNotification> defaultMailNotifications = List.of(
            NOTIFICATION_EMAIL_APPLICATION_APPLIED,
            NOTIFICATION_EMAIL_APPLICATION_ALLOWED,
            NOTIFICATION_EMAIL_APPLICATION_REVOKED,
            NOTIFICATION_EMAIL_APPLICATION_REJECTED,
            NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED,
            NOTIFICATION_EMAIL_APPLICATION_CANCELLATION,
            NOTIFICATION_EMAIL_APPLICATION_EDITED,
            NOTIFICATION_EMAIL_APPLICATION_CONVERTED,
            NOTIFICATION_EMAIL_APPLICATION_UPCOMING,
            NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT,
            NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER,
            NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT,
            NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER,
            NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT,
            NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT,
            NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED,
            NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED
        );

        final List<Role> defaultPermissions = List.of(
            USER
        );

        return createPerson(username, firstName, lastName, email, defaultMailNotifications, defaultPermissions);
    }

    @Override
    @Transactional
    public Person create(String username, String firstName, String lastName, String email,
                         List<MailNotification> notifications, List<Role> permissions) {

        return createPerson(username, firstName, lastName, email, notifications, permissions);
    }

    private Person createPerson(String username, String firstName, String lastName, String email,
                                List<MailNotification> notifications, List<Role> permissions) {

        final Person person = new Person(username.strip(), lastName.strip(), firstName.strip(), email.strip());
        person.setCreatedAt(Instant.now(clock));
        person.setNotifications(notifications);
        person.setPermissions(permissions);

        final Person createdPerson = personRepository.save(person);
        LOG.info("Created person: {}", createdPerson);

        accountInteractionService.createDefaultAccount(createdPerson);
        workingTimeWriteService.createDefaultWorkingTime(createdPerson);

        applicationEventPublisher.publishEvent(toPersonCreatedEvent(createdPerson));

        return createdPerson;
    }

    @Override
    @Transactional
    public Person update(PersonId personId, PersonUpdate personUpdate) {

        final Person person = personRepository.findById(personId.value())
            .orElseThrow(() -> new IllegalArgumentException("Can not find a person for ID = " + personId.value()));

        final Collection<Role> previousPermissions = List.copyOf(person.getPermissions());

        personUpdate.personalData().ifPresent(personalData -> {
            person.setUsername(personalData.username());
            person.setFirstName(personalData.firstName());
            person.setLastName(personalData.lastName());
            person.setEmail(personalData.email());
        });
        personUpdate.permissions().ifPresent(person::setPermissions);
        personUpdate.notifications().ifPresent(person::setNotifications);

        final Person updatedPerson = personRepository.save(person);
        LOG.info("Updated person: {}", updatedPerson);

        if (updatedPerson.isInactive()) {
            applicationEventPublisher.publishEvent(toPersonDisabledEvent(updatedPerson));
        }

        applicationEventPublisher.publishEvent(toPersonUpdateEvent(updatedPerson));

        publishPermissionsChangedEventIfNecessary(updatedPerson, previousPermissions);

        return updatedPerson;
    }

    @Override
    @Transactional
    public void delete(PersonId personId, PersonId signedInUserId) {

        final Person person = personRepository.findById(personId.value())
            .orElseThrow(() -> new IllegalArgumentException("Can not find a person for ID = " + personId.value()));

        applicationEventPublisher.publishEvent(new PersonDeletedEvent(person));
        accountInteractionService.deleteAllByPerson(person);
        workingTimeWriteService.deleteAllByPerson(person);
        personRepository.delete(person);

        final String status = person.isActive() ? "active" : "inactive";
        LOG.info("person with id {} ({}) and status {} deleted by signed in user with id {}", person.getId(), person.getUsername(), status, signedInUserId.value());
    }

    @Override
    public Optional<Person> getPersonByID(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Optional<Person> getPersonByUsername(String username) {
        return personRepository.findByUsernameIgnoreCase(username);
    }

    @Override
    public Optional<Person> getPersonByMailAddress(String mailAddress) {
        return personRepository.findByEmailIgnoreCase(mailAddress);
    }

    @Override
    public List<Person> getActivePersons() {
        return personRepository.findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(INACTIVE);
    }

    @Override
    public List<Person> getInactivePersons() {
        return personRepository.findByPermissionsContainingOrderByFirstNameAscLastNameAsc(INACTIVE);
    }

    @Override
    public List<Person> getAllPersons() {
        return personRepository.findAllByOrderByIdAsc();
    }

    @Override
    public List<Person> getAllPersonsByIds(Collection<PersonId> personIds) {
        return personRepository.findAllByIdIsInOrderByFirstNameAscLastNameAsc(personIds.stream().map(PersonId::value).toList());
    }

    @Override
    public List<Person> getAllPersonsHavingAccountInYear(Year year) {
        return personRepository.findAllWithAccountByYear(year.getValue());
    }

    @Override
    public Page<Person> getActivePersons(PersonPageable personPageable, String query) {
        return personRepository.findByPermissionsNotContainingAndByNiceNameContainingIgnoreCase(INACTIVE, query, personPageable.toPageable());
    }

    @Override
    public List<Person> getActivePersonsByRole(final Role role) {
        return personRepository.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(role, INACTIVE);
    }

    @Override
    public List<Person> getActivePersonsWithNotificationType(final MailNotification notification) {
        return personRepository.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(INACTIVE, notification);
    }

    @Override
    public Page<Person> getInactivePersons(PersonPageable pageable, String query) {
        return personRepository.findByPermissionsContainingAndNiceNameContainingIgnoreCase(INACTIVE, query, pageable.toPageable());
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

        final Person savedPerson = personRepository.save(person);

        LOG.info("Add 'OFFICE' role to person: {}", person);

        return savedPerson;
    }

    @Override
    public int numberOfActivePersons() {
        return personRepository.countByPermissionsNotContaining(INACTIVE);
    }

    @Override
    public int numberOfPersonsWithOfficeRoleExcludingPerson(long excludingId) {
        return personRepository.countByPermissionsContainingAndIdNotIn(OFFICE, List.of(excludingId));
    }

    private void publishPermissionsChangedEventIfNecessary(Person updatedPerson, Collection<Role> previousPermissions) {
        final Collection<Role> currentPermissions = updatedPerson.getPermissions();
        if (!new HashSet<>(previousPermissions).equals(new HashSet<>(currentPermissions))) {
            applicationEventPublisher.publishEvent(
                PersonPermissionsChangedEvent.of(updatedPerson, previousPermissions, currentPermissions)
            );
        }
    }

    private PersonCreatedEvent toPersonCreatedEvent(Person person) {
        return new PersonCreatedEvent(this, person.getId(), person.getNiceName(), person.getUsername(), person.getEmail(), person.isActive(), person.getCreatedAt());
    }

    private PersonUpdatedEvent toPersonUpdateEvent(Person person) {
        return new PersonUpdatedEvent(this, person.getId(), person.getNiceName(), person.getUsername(), person.getEmail(), person.isActive());
    }

    private PersonDisabledEvent toPersonDisabledEvent(Person person) {
        return new PersonDisabledEvent(this, person.getId(), person.getNiceName(), person.getUsername(), person.getEmail());
    }
}
