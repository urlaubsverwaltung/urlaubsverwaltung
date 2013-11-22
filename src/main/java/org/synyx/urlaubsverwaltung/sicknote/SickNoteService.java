package org.synyx.urlaubsverwaltung.sicknote;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.Comment;
import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.CommentService;
import org.synyx.urlaubsverwaltung.application.web.AppForm;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteComment;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentDAO;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteStatus;

import java.math.BigDecimal;

import java.util.List;


/**
 * Service for handling {@link SickNote}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Transactional
public class SickNoteService {

    private SickNoteDAO sickNoteDAO;
    private SickNoteCommentDAO commentDAO;
    private OwnCalendarService calendarService;
    private ApplicationService applicationService;
    private CommentService commentService;

    @Autowired
    public SickNoteService(SickNoteDAO sickNoteDAO, SickNoteCommentDAO commentDAO, OwnCalendarService calendarService,
        ApplicationService applicationService, CommentService commentService) {

        this.sickNoteDAO = sickNoteDAO;
        this.commentDAO = commentDAO;
        this.calendarService = calendarService;
        this.applicationService = applicationService;
        this.commentService = commentService;
    }


    public SickNoteService() {
    }

    public void save(SickNote sickNote) {

        sickNote.setLastEdited(DateMidnight.now());

        sickNoteDAO.save(sickNote);
    }


    public void touch(SickNote sickNote, SickNoteStatus status, Person loggedUser) {

        setWorkDays(sickNote);
        save(sickNote);

        SickNoteComment comment = new SickNoteComment();
        addComment(sickNote.getId(), comment, status, loggedUser);
    }


    public void setWorkDays(SickNote sickNote) {

        BigDecimal workDays;

        if (sickNote.getStartDate() != null && sickNote.getEndDate() != null) {
            workDays = calendarService.getWorkDays(DayLength.FULL, sickNote.getStartDate(), sickNote.getEndDate());
        } else {
            workDays = BigDecimal.ZERO;
        }

        sickNote.setWorkDays(workDays);
    }


    public void addComment(Integer sickNoteId, SickNoteComment comment, SickNoteStatus status, Person author) {

        SickNote sickNote = getById(sickNoteId);

        comment.setDate(DateMidnight.now());
        comment.setStatus(status);
        comment.setPerson(author);

        commentDAO.save(comment);

        sickNote.addComment(comment);

        save(sickNote);
    }


    public List<SickNote> getAll() {

        return sickNoteDAO.findAll();
    }


    public SickNote getById(Integer id) {

        return sickNoteDAO.findOne(id);
    }


    public List<SickNote> getByPersonAndPeriod(Person person, DateMidnight from, DateMidnight to) {

        return sickNoteDAO.findByPersonAndPeriod(person, from.toDate(), to.toDate());
    }


    public List<SickNote> getByPeriod(DateMidnight from, DateMidnight to) {

        return sickNoteDAO.findByPeriod(from.toDate(), to.toDate());
    }


    public void convertSickNoteToVacation(AppForm appForm, SickNote sickNote, Person loggedUser) {

        appForm.setHowLong(DayLength.FULL);

        Application application = appForm.createApplicationObject();

        applicationService.apply(application, sickNote.getPerson(), loggedUser);

        application.setStatus(ApplicationStatus.ALLOWED);
        application.setEditedDate(DateMidnight.now());

        applicationService.signApplicationByUser(application, loggedUser);
        applicationService.save(application);

        commentService.saveComment(new Comment(), loggedUser, application);

        adjustSickNote(sickNote, application);

        save(sickNote);

        SickNoteComment sickNoteComment = new SickNoteComment();
        addComment(sickNote.getId(), sickNoteComment, SickNoteStatus.CONVERTED_TO_VACATION, loggedUser);
    }


    protected void adjustSickNote(SickNote sickNote, Application application) {

        SickNoteConversion conversion = new SickNoteConversion(sickNote, application);

        if (conversion.identicalRange()) {
            sickNote.setWorkDays(BigDecimal.ZERO);
        } else if (conversion.onlyStartIsEqual()) {
            sickNote.setStartDate(application.getEndDate().plusDays(1));
            setWorkDays(sickNote);
        } else if (conversion.onlyEndIsEqual()) {
            sickNote.setEndDate(application.getStartDate().minusDays(1));
            setWorkDays(sickNote);
        } else {
            // overlapping case not implemented at the moment
            // sick note start date != application start date AND sick note end date != application end date
        }
    }
}
