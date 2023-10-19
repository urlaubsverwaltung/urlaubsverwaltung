package org.synyx.urlaubsverwaltung.application.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

/**
 * Implementation of interface {@link ApplicationCommentService}.
 */
@Service
@Transactional
class ApplicationCommentServiceImpl implements ApplicationCommentService {

    private final ApplicationCommentRepository commentRepository;
    private final ApplicationService applicationService;
    private final Clock clock;

    @Autowired
    ApplicationCommentServiceImpl(ApplicationCommentRepository commentRepository, ApplicationService applicationService,
                                  Clock clock) {

        this.commentRepository = commentRepository;
        this.applicationService = applicationService;
        this.clock = clock;
    }

    @Override
    public ApplicationComment create(Application application, ApplicationCommentAction action, Optional<String> text,
                                     Person author) {

        final ApplicationCommentEntity commentEntity = new ApplicationCommentEntity(author, clock);
        commentEntity.setAction(action);
        commentEntity.setApplicationId(application.getId());
        text.ifPresent(commentEntity::setText);

        final ApplicationCommentEntity savedEntity = commentRepository.save(commentEntity);

        final LongFunction<Application> byId = applicationId -> applicationService.getApplicationById(applicationId)
            .orElseThrow(() -> new IllegalStateException("could not find application with id=" + applicationId));

        return toApplicationComment(savedEntity, byId);
    }


    @Override
    public List<ApplicationComment> getCommentsByApplication(Application application) {

        final List<ApplicationCommentEntity> commentEntities = commentRepository.findByApplicationId(application.getId());

        final List<Long> applicationIds = commentEntities
            .stream()
            .map(ApplicationCommentEntity::getApplicationId)
            .distinct()
            .toList();

        final Map<Long, Application> applicationById = applicationService.findApplicationsByIds(applicationIds)
            .stream()
            .collect(Collectors.toMap(Application::getId, Function.identity()));

        return commentEntities.stream().map(entity -> toApplicationComment(entity, applicationById::get)).toList();
    }

    @Override
    public void deleteByApplicationPerson(Person applicationPerson) {
        commentRepository.deleteByApplicationPerson(applicationPerson);
    }

    @Override
    public void deleteCommentAuthor(Person author) {
        final List<ApplicationCommentEntity> applicationComments = commentRepository.findByPerson(author);
        applicationComments.forEach(applicationComment -> applicationComment.setPerson(null));
        commentRepository.saveAll(applicationComments);
    }

    private ApplicationComment toApplicationComment(ApplicationCommentEntity entity, LongFunction<Application> applicationSupplier) {
        final Application application = applicationSupplier.apply(entity.getApplicationId());
        return new ApplicationComment(entity.getId(), entity.getDate(), application, entity.getAction(), entity.getPerson(), entity.getText());
    }
}
