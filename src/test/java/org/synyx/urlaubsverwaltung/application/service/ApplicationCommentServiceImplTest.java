package org.synyx.urlaubsverwaltung.application.service;

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

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationCommentAction.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationCommentAction.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.application.service.ApplicationCommentServiceImpl}.
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
        final VacationType vacationType = createVacationType(HOLIDAY);
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
        final VacationType vacationType = createVacationType(HOLIDAY);
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
}
