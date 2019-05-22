package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteTypeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.util.Optional;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class SickNoteControllerTest {

    private SickNoteController sut;

    @Mock
    private SickNoteService sickNoteServiceMock;
    @Mock
    private SickNoteInteractionService sickNoteInteractionServiceMock;
    @Mock
    private SickNoteCommentService sickNoteCommentServiceMock;
    @Mock
    private SickNoteTypeService sickNoteTypeServiceMock;
    @Mock
    private VacationTypeService vacationTypeServiceMock;
    @Mock
    private PersonService personServiceMock;
    @Mock
    private WorkDaysService calendarServiceMock;
    @Mock
    private SickNoteValidator validatorMock;
    @Mock
    private SickNoteConvertFormValidator sickNoteConvertFormValidatorMock;


    @Before
    public void setUp() {

        sut = new SickNoteController(sickNoteServiceMock,
            sickNoteInteractionServiceMock, sickNoteCommentServiceMock, sickNoteTypeServiceMock,
            vacationTypeServiceMock, personServiceMock, calendarServiceMock, validatorMock,
            sickNoteConvertFormValidatorMock);
    }

    @Test
    public void ensureNewHasCorrectModelAttributes() throws Exception {
        final ResultActions resultActions = perform(get("/web/sicknote/new"));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("sickNote", instanceOf(SickNoteForm.class)));
        resultActions.andExpect(model().attribute("persons", personServiceMock.getActivePersons()));
        resultActions.andExpect(model().attribute("sickNoteTypes", sickNoteTypeServiceMock.getSickNoteTypes()));
        resultActions.andExpect(view().name("sicknote/sick_note_form"));
    }

    @Test
    public void ensureEditHasCorrectModelAttributes() throws Exception {
        SickNote sickNote = new SickNote();
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        when(sickNoteServiceMock.getById(0)).thenReturn(Optional.of(sickNote));

        final ResultActions resultActions = perform(get("/web/sicknote/0/edit"));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("sickNote", instanceOf(SickNoteForm.class)));
        resultActions.andExpect(model().attribute("sickNoteTypes", sickNoteTypeServiceMock.getSickNoteTypes()));
        resultActions.andExpect(view().name("sicknote/sick_note_form"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
