package org.synyx.urlaubsverwaltung.core.overtime;

import org.joda.time.DateMidnight;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OvertimeInteractionServiceImplTest {

    private OvertimeInteractionService overtimeInteractionService;

    private OvertimeService overtimeService;
    private OvertimeCommentService overtimeCommentService;

    @Before
    public void setUp() {

        overtimeService = Mockito.mock(OvertimeService.class);
        overtimeCommentService = Mockito.mock(OvertimeCommentService.class);

        overtimeInteractionService = new OvertimeInteractionServiceImpl(overtimeService, overtimeCommentService);
    }


    @Test
    public void ensureSavesOvertimeAndComment() {

        Person creator = TestDataCreator.createPerson();

        Person person = TestDataCreator.createPerson();
        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(3);
        Optional<String> text = Optional.of("Foo Bar");

        Overtime overtime = new Overtime(person, startDate, endDate, BigDecimal.ONE);

        overtimeInteractionService.record(overtime, text, creator);

        Mockito.verify(overtimeService).save(overtime);
        Mockito.verify(overtimeCommentService).create(overtime, OvertimeAction.CREATED, text, creator);
    }
}
