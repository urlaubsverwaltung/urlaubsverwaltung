package org.synyx.urlaubsverwaltung.overtime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.math.RoundingMode.UNNECESSARY;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OvertimeDAOIT {

    @Autowired
    private PersonService personService;

    @Autowired
    private OvertimeDAO overtimeDAO;

    @Test
    public void ensureCanPersistOvertime() {

        final Person person = TestDataCreator.createPerson();
        final Person savedPerson = personService.save(person);

        final LocalDate now = LocalDate.now(UTC);
        final Overtime overtime = new Overtime(savedPerson, now, now.plusDays(2), BigDecimal.ONE);
        assertThat(overtime.getId()).isNull();

        overtimeDAO.save(overtime);

        assertThat(overtime.getId()).isNotNull();
    }


    @Test
    public void ensureCountsTotalHoursCorrectly() {

        final Person person = TestDataCreator.createPerson("sam", "sam", "smith", "smith@test.de");
        final Person savedPerson = personService.save(person);

        final Person otherPerson = TestDataCreator.createPerson("freddy", "freddy", "Gwin", "gwin@test.de");
        final Person savedOtherPerson = personService.save(otherPerson);

        LocalDate now = LocalDate.now(UTC);

        // Overtime for person
        overtimeDAO.save(new Overtime(savedPerson, now, now.plusDays(2), new BigDecimal("3")));
        overtimeDAO.save(new Overtime(savedPerson, now.plusDays(5), now.plusDays(10), new BigDecimal("0.5")));
        overtimeDAO.save(new Overtime(savedPerson, now.minusDays(8), now.minusDays(4), new BigDecimal("-1")));

        // Overtime for other person
        overtimeDAO.save(new Overtime(savedOtherPerson, now.plusDays(5), now.plusDays(10), new BigDecimal("5")));

        BigDecimal totalHours = overtimeDAO.calculateTotalHoursForPerson(person);

        assertThat(totalHours).isNotNull();
        assertThat(totalHours.setScale(1, UNNECESSARY)).isEqualTo(new BigDecimal("2.5").setScale(1,
            UNNECESSARY));
    }


    @Test
    public void ensureReturnsNullAsTotalOvertimeIfPersonHasNoOvertimeRecords() {

        Person person = TestDataCreator.createPerson();
        personService.save(person);

        BigDecimal totalHours = overtimeDAO.calculateTotalHoursForPerson(person);

        assertThat(totalHours).isNull();
    }


    @Test
    public void ensureReturnsAllRecordsWithStartOrEndDateInTheGivenYear() {

        final Person person = TestDataCreator.createPerson();
        final Person savedPerson = personService.save(person);

        // records for 2015
        overtimeDAO.save(new Overtime(savedPerson, LocalDate.of(2014, 12, 30), LocalDate.of(2015, 1, 3),
            new BigDecimal("1")));
        overtimeDAO.save(new Overtime(savedPerson, LocalDate.of(2015, 10, 5), LocalDate.of(2015, 10, 20),
            new BigDecimal("2")));
        overtimeDAO.save(new Overtime(savedPerson, LocalDate.of(2015, 12, 28), LocalDate.of(2016, 1, 6),
            new BigDecimal("3")));

        // record for 2014
        overtimeDAO.save(new Overtime(savedPerson, LocalDate.of(2014, 12, 5), LocalDate.of(2014, 12, 31),
            new BigDecimal("4")));

        List<Overtime> records = overtimeDAO.findByPersonAndPeriod(savedPerson,
            LocalDate.of(2015, 1, 1), LocalDate.of(2015, 12, 31));

        assertThat(records).isNotNull();
        assertThat(records.size()).isEqualTo(3);
        assertThat(records.get(0).getHours()).isEqualTo(BigDecimal.valueOf(1));
        assertThat(records.get(1).getHours()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(records.get(2).getHours()).isEqualTo(BigDecimal.valueOf(3));
    }
}
