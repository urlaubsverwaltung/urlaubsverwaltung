package org.synyx.urlaubsverwaltung.application.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
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

    private ApplicationCommentServiceImpl sut;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationCommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        sut = new ApplicationCommentServiceImpl(commentRepository, applicationService, Clock.systemUTC());
    }


    @Test
    void ensureCreatesACommentAndPersistsIt() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final VacationType<?> vacationType = TestDataCreator.createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Application application = createApplication(person, vacationType);
        application.setId(1337L);

        when(commentRepository.save(any())).then(returnsFirstArg());
        when(applicationService.getApplicationById(1337L)).thenReturn(Optional.of(application));

        final Person author = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationComment comment = sut.create(application, ALLOWED, Optional.empty(), author);

        assertThat(comment).isNotNull();
        assertThat(comment.date()).isNotNull();
        assertThat(comment.application()).isNotNull();
        assertThat(comment.action()).isEqualTo(ALLOWED);
        assertThat(comment.person()).isEqualTo(author);
        assertThat(comment.text()).isNull();

        final ArgumentCaptor<ApplicationCommentEntity> captor = ArgumentCaptor.forClass(ApplicationCommentEntity.class);
        verify(commentRepository).save(captor.capture());

        assertThat(captor.getValue()).satisfies(persistedComment -> {
            assertThat(persistedComment.getDate()).isNotNull();
            assertThat(persistedComment.getApplicationId()).isEqualTo(1337L);
            assertThat(persistedComment.getAction()).isEqualTo(ALLOWED);
            assertThat(persistedComment.getPerson()).isEqualTo(author);
            assertThat(persistedComment.getText()).isNull();
        });
    }


    @Test
    void ensureCreationOfCommentWithTextWorks() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final VacationType<?> vacationType = TestDataCreator.createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Application application = createApplication(person, vacationType);
        application.setId(42L);

        when(commentRepository.save(any())).then(returnsFirstArg());
        when(applicationService.getApplicationById(42L)).thenReturn(Optional.of(application));

        final Person author = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final ApplicationComment savedComment = sut.create(application, REJECTED, Optional.of("Foo"), author);

        assertThat(savedComment.text()).isEqualTo("Foo");

        final ArgumentCaptor<ApplicationCommentEntity> captor = ArgumentCaptor.forClass(ApplicationCommentEntity.class);
        verify(commentRepository).save(captor.capture());

        assertThat(captor.getValue()).satisfies(persistedComment -> {
            assertThat(persistedComment.getText()).isEqualTo("Foo");
        });
    }

    @Test
    void ensureDeletionOfCommentAuthor() {

        final Person person = new Person();
        person.setId(1L);

        final Application application = new Application();
        application.setId(1337L);

        final ApplicationCommentEntity entity = new ApplicationCommentEntity();
        entity.setId(1L);
        entity.setPerson(person);

        when(commentRepository.findByPerson(person)).thenReturn(List.of(entity));

        sut.deleteCommentAuthor(person);

        verify(commentRepository).findByPerson(person);

        final ArgumentCaptor<List<ApplicationCommentEntity>> argument = ArgumentCaptor.forClass(List.class);
        verify(commentRepository).saveAll(argument.capture());
        assertThat(argument.getValue().get(0).getPerson()).isNull();
    }

    @Test
    void ensureDeletionByApplicationPerson() {

        final Person person = new Person();
        person.setId(1L);

        sut.deleteByApplicationPerson(person);

        verify(commentRepository).deleteByApplicationPerson(person);
    }
}
