package org.synyx.urlaubsverwaltung.application.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.REJECTED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;


/**
 * Unit test for {@link ApplicationCommentServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationCommentServiceImplTest {

    private ApplicationCommentServiceImpl commentService;

    @Mock
    private ApplicationCommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentService = new ApplicationCommentServiceImpl(commentRepository, Clock.systemUTC());
    }


    @Test
    void ensureCreatesACommentAndPersistsIt() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final VacationTypeEntity vacationType = TestDataCreator.createVacationTypeEntity(HOLIDAY);
        final Application application = createApplication(person, vacationType);

        when(commentRepository.save(any())).then(returnsFirstArg());

        final Person author = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationComment comment = commentService.create(application, ALLOWED, Optional.empty(), author);

        assertThat(comment).isNotNull();
        assertThat(comment.getDate()).isNotNull();
        assertThat(comment.getAction()).isNotNull();
        assertThat(comment.getPerson()).isNotNull();
        assertThat(comment.getApplication()).isNotNull();
        assertThat(comment.getAction()).isEqualTo(ALLOWED);
        assertThat(comment.getPerson()).isEqualTo(author);
        assertThat(comment.getText()).isNull();

        verify(commentRepository).save(comment);
    }


    @Test
    void ensureCreationOfCommentWithTextWorks() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final VacationTypeEntity vacationType = TestDataCreator.createVacationTypeEntity(HOLIDAY);
        final Application application = createApplication(person, vacationType);

        when(commentRepository.save(any())).then(returnsFirstArg());

        final Person author = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationComment savedComment = commentService.create(application, REJECTED, Optional.of("Foo"), author);

        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getDate()).isNotNull();
        assertThat(savedComment.getAction()).isNotNull();
        assertThat(savedComment.getPerson()).isNotNull();
        assertThat(savedComment.getText()).isNotNull();
        assertThat(savedComment.getAction()).isEqualTo(REJECTED);
        assertThat(savedComment.getPerson()).isEqualTo(author);
        assertThat(savedComment.getText()).isEqualTo("Foo");

        verify(commentRepository).save(savedComment);
    }

    @Test
    void ensureDeletionOfCommentAuthor() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final ApplicationComment applicationComment = new ApplicationComment(person, Clock.fixed(Instant.ofEpochSecond(0), ZoneId.systemDefault()));
        applicationComment.setId(1);
        final List<ApplicationComment> applicationCommentsOfAuthor = List.of(applicationComment);
        when(commentRepository.findByPerson(person)).thenReturn(applicationCommentsOfAuthor);

        commentService.deleteCommentAuthor(person);

        verify(commentRepository).findByPerson(person);

        final ArgumentCaptor<List<ApplicationComment>> argument = ArgumentCaptor.forClass(List.class);
        verify(commentRepository).saveAll(argument.capture());
        assertThat(argument.getValue().get(0).getPerson()).isNull();
    }
}
