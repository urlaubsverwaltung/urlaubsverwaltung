package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.security.core.GrantedAuthority;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.util.Arrays;
import java.util.Collection;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.security.PersonContextMapper}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonContextMapperTest {

    private PersonContextMapper personContextMapper;

    private PersonService personService;
    private MailService mailService;

    @Before
    public void setUp() {

        personService = Mockito.mock(PersonService.class);
        mailService = Mockito.mock(MailService.class);

        personContextMapper = new PersonContextMapper(personService, mailService, false);
    }


    @Test
    public void ensureReturnsEmptyListOfAuthoritiesForNullPerson() {

        Collection<GrantedAuthority> authorities = personContextMapper.getGrantedAuthorities(null);

        Assert.assertNotNull("Should not be null", authorities);
        Assert.assertTrue("Should be empty", authorities.isEmpty());
    }


    @Test
    public void ensureReturnsCorrectListOfAuthoritiesUsingTheRolesOfTheGivenPerson() {

        Person person = new Person();
        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Collection<GrantedAuthority> authorities = personContextMapper.getGrantedAuthorities(person);

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("No authority for user role found",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.USER));
        Assert.assertTrue("No authority for boss role found",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.BOSS));
    }


    @Test
    public void ensureFirstCreatedPersonHasTheCorrectRoles() {

        Person person = personContextMapper.createPerson("murygina", true);

        Collection<Role> roles = person.getPermissions();

        Assert.assertEquals("Wrong number of roles", 2, roles.size());
        Assert.assertTrue("Does not contain user role", roles.contains(Role.USER));
        Assert.assertTrue("Does not contain office role", roles.contains(Role.OFFICE));

        Assert.assertTrue("Should be active", person.isActive());
    }

    @Test
    public void ensureFurtherCreatedPersonHasTheCorrectRoles() {

        Person person = personContextMapper.createPerson("murygina2", false);

        Collection<Role> roles = person.getPermissions();

        Assert.assertEquals("Wrong number of roles", 1, roles.size());
        Assert.assertTrue("Does not contain user role", roles.contains(Role.USER));

        Assert.assertTrue("Should be active", person.isActive());
    }


    @Test
    public void ensureCreatedPersonIsSaved() {

        personContextMapper.createPerson("murygina", true);

        Mockito.verify(personService).save(Mockito.any(Person.class));
    }
}
