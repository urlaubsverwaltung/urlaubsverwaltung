package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;

@ExtendWith(MockitoExtension.class)
class OvertimeAbsenceApiControllerTest {

    private OvertimeAbsenceApiController sut;
    @Mock
    private ApplicationService applicationService;


    @BeforeEach
    void setUp() {
        sut = new OvertimeAbsenceApiController(applicationService);
    }

    @Test
    void threeHoursForty() throws Exception {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(2L);
        long applicationId = 3L;
        final Application application = new Application();
        application.setId(applicationId);
        application.setPerson(person);
        application.setHours(Duration.ofHours(3).plusMinutes(40));
        application.setVacationType(createVacationType(1L, OVERTIME, new StaticMessageSource()));
        when(applicationService.getApplicationById(applicationId)).thenReturn(Optional.of(application));

        perform(get("/api/persons/2/absences/3/overtime"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "id": 3,
                    "duration": "PT3H40M"
                }
                """));

    }

    @Test
    void wrongPerson() throws Exception {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(2L);
        long applicationId = 3L;
        final Application application = new Application();
        application.setId(applicationId);
        application.setPerson(person);
        application.setHours(Duration.ofHours(3));
        application.setVacationType(createVacationType(1L, OVERTIME, new StaticMessageSource()));
        when(applicationService.getApplicationById(applicationId)).thenReturn(Optional.of(application));

        perform(get("/api/persons/1/absences/3/overtime"))
            .andExpect(status().isNotFound());
    }

    @Test
    void nonExistentApplication() throws Exception {
        perform(get("/api/persons/2/absences/3/overtime"))
            .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @EnumSource(value = VacationCategory.class, names = "OVERTIME", mode = EnumSource.Mode.EXCLUDE)
    void differentVacationCategory(final VacationCategory category) throws Exception {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(2L);
        long applicationId = 3L;
        final Application application = new Application();
        application.setId(applicationId);
        application.setPerson(person);
        application.setHours(Duration.ofHours(3));
        application.setVacationType(createVacationType(1L, category, new StaticMessageSource()));
        when(applicationService.getApplicationById(applicationId)).thenReturn(Optional.of(application));

        perform(get("/api/persons/2/absences/3/overtime"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void durationNotKnown() throws Exception {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(2L);
        long applicationId = 3L;
        final Application application = new Application();
        application.setId(applicationId);
        application.setPerson(person);
        application.setVacationType(createVacationType(1L, OVERTIME, new StaticMessageSource()));
        when(applicationService.getApplicationById(applicationId)).thenReturn(Optional.of(application));

        perform(get("/api/persons/2/absences/3/overtime"))
            .andExpect(status().isInternalServerError());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }

}
