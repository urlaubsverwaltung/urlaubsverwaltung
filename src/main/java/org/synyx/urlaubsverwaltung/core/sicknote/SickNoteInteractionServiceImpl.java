package org.synyx.urlaubsverwaltung.core.sicknote;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.CommentService;
import org.synyx.urlaubsverwaltung.core.application.service.SignService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteStatus;

import java.util.Optional;


/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNoteInteractionService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Transactional
public class SickNoteInteractionServiceImpl implements SickNoteInteractionService {

    private static final boolean ACTIVE = true;
    private static final boolean INACTIVE = false;

    private final SickNoteService sickNoteService;
    private final SickNoteCommentService sickNoteCommentService;
    private final ApplicationService applicationService;
    private final CommentService applicationCommentService;
    private final SignService signService;
    private final MailService mailService;

    @Autowired
    public SickNoteInteractionServiceImpl(SickNoteService sickNoteService,
        SickNoteCommentService sickNoteCommentService, ApplicationService applicationService,
        CommentService applicationCommentService, SignService signService, MailService mailService) {

        this.sickNoteService = sickNoteService;
        this.sickNoteCommentService = sickNoteCommentService;
        this.applicationService = applicationService;
        this.applicationCommentService = applicationCommentService;
        this.signService = signService;
        this.mailService = mailService;
    }

    @Override
    public SickNote create(SickNote sickNote, Person creator) {

        sickNote.setActive(ACTIVE);
        sickNote.setLastEdited(DateMidnight.now());

        sickNoteService.save(sickNote);
        sickNoteCommentService.create(sickNote, SickNoteStatus.CREATED, Optional.<String>empty(), creator);

        return sickNote;
    }


    @Override
    public SickNote update(SickNote sickNote, Person editor) {

        sickNote.setActive(ACTIVE);
        sickNote.setLastEdited(DateMidnight.now());

        sickNoteService.save(sickNote);
        sickNoteCommentService.create(sickNote, SickNoteStatus.EDITED, Optional.<String>empty(), editor);

        return sickNote;
    }


    @Override
    public SickNote convert(SickNote sickNote, Application application, Person converter) {

        // create an application for leave that is allowed directly
        application.setApplier(converter);
        application.setStatus(ApplicationStatus.ALLOWED);

        signService.signApplicationByBoss(application, converter);
        applicationService.save(application);
        applicationCommentService.create(application, ApplicationStatus.ALLOWED, Optional.<String>empty(), converter);
        mailService.sendSickNoteConvertedToVacationNotification(application);

        // make sick note inactive
        sickNote.setActive(INACTIVE);
        sickNote.setLastEdited(DateMidnight.now());

        sickNoteService.save(sickNote);
        sickNoteCommentService.create(sickNote, SickNoteStatus.CONVERTED_TO_VACATION, Optional.<String>empty(),
            converter);

        return sickNote;
    }


    @Override
    public SickNote cancel(SickNote sickNote, Person canceller) {

        sickNote.setActive(INACTIVE);
        sickNote.setLastEdited(DateMidnight.now());

        sickNoteService.save(sickNote);
        sickNoteCommentService.create(sickNote, SickNoteStatus.CANCELLED, Optional.<String>empty(), canceller);

        return sickNote;
    }
}
