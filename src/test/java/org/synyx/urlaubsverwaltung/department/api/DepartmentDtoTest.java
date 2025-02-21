package org.synyx.urlaubsverwaltung.department.api;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.api.PersonDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentDtoTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void ensureDepartmentResponseCreatedCorrectly() {

        final Department department = new Department();

        final String expectedName = "Forschung";
        final String expectedDescription = "description";
        department.setName(expectedName);
        department.setDescription(expectedDescription);

        final String expectedLastModification = "2019-08-02";
        final LocalDate lastModification = LocalDate.parse(expectedLastModification, DATE_TIME_FORMATTER);
        department.setLastModification(lastModification);

        List<Person> members = new ArrayList<>();
        members.add(person("nils", "nils@net.com", "Nils", "Gruen"));
        members.add(person("nina", "nina@net.com", "Nina", "Link"));
        members.add(person("tim", "tim@net.com", "Tim", "Schwarz"));
        department.setMembers(members);

        List<Person> departmentHeads = new ArrayList<>();
        departmentHeads.add(person("departmentHead1", "departmentheadNils@net.com", "Nils", "Gruen"));
        departmentHeads.add(person("departmentHead2", "departmentheadNina@net.com", "Nina", "Link"));
        department.setDepartmentHeads(departmentHeads);

        DepartmentDto sut = new DepartmentDto(department);

        assertThat(sut.getName()).isEqualTo(expectedName);
        assertThat(sut.getDescription()).isEqualTo(expectedDescription);
        assertThat(sut.getLastModification()).isEqualTo(expectedLastModification);

        assertThat(sut.getMembers()).isNotNull();
        assertThat(sut.getMembers().getPersons()).hasSize(3);

        assertThat(sut.getDepartmentHeads()).isNotNull();
        assertThat(sut.getDepartmentHeads().getPersons()).hasSize(2);

        final List<PersonDto> memberPersons = sut.getMembers().getPersons();
        assertPersonResponseEqualsPerson(memberPersons.get(0), members.get(0));
        assertPersonResponseEqualsPerson(memberPersons.get(1), members.get(1));
        assertPersonResponseEqualsPerson(memberPersons.get(2), members.get(2));

        final List<PersonDto> departmentHeadPersons = sut.getDepartmentHeads().getPersons();
        assertPersonResponseEqualsPerson(departmentHeadPersons.get(0), departmentHeads.get(0));
        assertPersonResponseEqualsPerson(departmentHeadPersons.get(1), departmentHeads.get(1));
    }

    private void assertPersonResponseEqualsPerson(PersonDto personDto, Person person) {
        assertThat(personDto.getEmail()).isEqualTo(person.getEmail());
        assertThat(personDto.getFirstName()).isEqualTo(person.getFirstName());
        assertThat(personDto.getLastName()).isEqualTo(person.getLastName());
    }

    private Person person(String username, String email, String firstName, String lastName) {
        final Person person = new Person();
        person.setUsername(username);
        person.setEmail(email);
        person.setFirstName(firstName);
        person.setLastName(lastName);

        return person;
    }
}
