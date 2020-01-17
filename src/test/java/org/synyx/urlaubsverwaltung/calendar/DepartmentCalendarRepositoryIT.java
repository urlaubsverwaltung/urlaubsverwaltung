package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createDepartment;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DepartmentCalendarRepositoryIT {

    @Autowired
    private PersonCalendarRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    public void test() {

        final Person person = createPerson(randomAlphanumeric(10), "", "", "");
        final Person savedPerson = personService.save(person);
        final PersonCalendar personCalendar = new PersonCalendar(person);
        final PersonCalendar savedPersonCalendarConf = sut.save(personCalendar);

        final Optional<PersonCalendar> byId = sut.findById(personCalendar.getId());
        assertThat(byId.get().getPerson()).isEqualTo(person);
    }
}
