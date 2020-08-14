package org.synyx.urlaubsverwaltung.application.service;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.dao.ApplicationCommentRepository;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationAction.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationAction.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createVacationType;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.application.service.ApplicationCommentServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationCommentServiceImplTest {

    private ApplicationCommentService commentService;

    @Mock
    private ApplicationCommentRepository commentDAO;

    @BeforeEach
    void setUp() {
        commentService = new ApplicationCommentServiceImpl(commentDAO);
    }


    @Test
    void ensureCreatesACommentAndPersistsIt() {

        final Person person = createPerson("person");
        final VacationType vacationType = createVacationType(HOLIDAY);
        final Application application = createApplication(person, vacationType);

        when(commentDAO.save(any())).then(returnsFirstArg());

        final Person author = createPerson("author");
        final ApplicationComment comment = commentService.create(application, ALLOWED, Optional.empty(), author);

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertNotNull("Date should be set", comment.getDate());
        Assert.assertNotNull("Action should be set", comment.getAction());
        Assert.assertNotNull("Author should be set", comment.getPerson());
        Assert.assertNotNull("Application for leave should be set", comment.getApplication());

        Assert.assertEquals("Wrong action", ALLOWED, comment.getAction());
        Assert.assertEquals("Wrong author", author, comment.getPerson());

        Assert.assertNull("Text should not be set", comment.getText());

        verify(commentDAO).save(eq(comment));
    }


    @Test
    void ensureCreationOfCommentWithTextWorks() {

        final Person person = createPerson("person");
        final VacationType vacationType = createVacationType(HOLIDAY);
        final Application application = createApplication(person, vacationType);

        when(commentDAO.save(any())).then(returnsFirstArg());

        final Person author = createPerson("author");
        final ApplicationComment savedComment = commentService.create(application, REJECTED, Optional.of("Foo"), author);

        Assert.assertNotNull("Should not be null", savedComment);

        Assert.assertNotNull("Date should be set", savedComment.getDate());
        Assert.assertNotNull("Action should be set", savedComment.getAction());
        Assert.assertNotNull("Author should be set", savedComment.getPerson());
        Assert.assertNotNull("Text should be set", savedComment.getText());

        Assert.assertEquals("Wrong action", REJECTED, savedComment.getAction());
        Assert.assertEquals("Wrong author", author, savedComment.getPerson());
        Assert.assertEquals("Wrong text", "Foo", savedComment.getText());

        verify(commentDAO).save(eq(savedComment));
    }
}
