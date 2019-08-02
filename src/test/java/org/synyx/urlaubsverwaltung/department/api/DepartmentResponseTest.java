package org.synyx.urlaubsverwaltung.department.api;

import org.junit.Test;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DepartmentResponseTest {

    private DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    public void ensureDepartmentResponseCreatedCorrectly() {

        final Department department = new Department();

        final String expectedName = "Forschung";
        department.setName(expectedName);
        department.setDescription("description");

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

        DepartmentResponse sut = new DepartmentResponse(department);

        assertThat(sut.getName()).isEqualTo(expectedName);
        // DepartmentResponse uses Department.name for description - don't know if this is intended
        assertThat(sut.getDescription()).isEqualTo(expectedName);
        assertThat(sut.getLastModification()).isEqualTo(expectedLastModification);

        assertThat(sut.getMembers()).isNotNull();
        assertThat(sut.getMembers().getPersons()).hasSize(3);

        assertThat(sut.getDepartmentHeads()).isNotNull();
        assertThat(sut.getDepartmentHeads().getPersons()).hasSize(2);

        for (int i = 0; i < 3; i++) {
            assertThat(sut.getMembers().getPersons().get(i).getLdapName()).isEqualTo(members.get(i).getLoginName());
            assertThat(sut.getMembers().getPersons().get(i).getEmail()).isEqualTo(members.get(i).getEmail());
            assertThat(sut.getMembers().getPersons().get(i).getFirstName()).isEqualTo(members.get(i).getFirstName());
            assertThat(sut.getMembers().getPersons().get(i).getLastName()).isEqualTo(members.get(i).getLastName());
        }

        for (int i = 0; i < 2; i++) {
            assertThat(sut.getDepartmentHeads().getPersons().get(i).getLdapName()).isEqualTo(departmentHeads.get(i).getLoginName());
            assertThat(sut.getDepartmentHeads().getPersons().get(i).getEmail()).isEqualTo(departmentHeads.get(i).getEmail());
            assertThat(sut.getDepartmentHeads().getPersons().get(i).getFirstName()).isEqualTo(departmentHeads.get(i).getFirstName());
            assertThat(sut.getDepartmentHeads().getPersons().get(i).getLastName()).isEqualTo(departmentHeads.get(i).getLastName());
        }
    }

    private Person person(String loginName, String email, String firstName, String lastName) {

        final Person person = new Person();

        person.setLoginName(loginName);
        person.setEmail(email);
        person.setFirstName(firstName);
        person.setLastName(lastName);

        return person;
    }

}
