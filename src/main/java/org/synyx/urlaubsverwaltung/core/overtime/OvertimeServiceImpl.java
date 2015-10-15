package org.synyx.urlaubsverwaltung.core.overtime;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
@Transactional
@Service
public class OvertimeServiceImpl implements OvertimeService {

    private static final Logger LOG = Logger.getLogger(OvertimeServiceImpl.class);

    private final OvertimeDAO overtimeDAO;
    private final OvertimeCommentDAO commentDAO;

    @Autowired
    public OvertimeServiceImpl(OvertimeDAO overtimeDAO, OvertimeCommentDAO commentDAO) {

        this.overtimeDAO = overtimeDAO;
        this.commentDAO = commentDAO;
    }

    @Override
    public List<Overtime> getOvertimeRecordsForPerson(Person person) {

        Assert.notNull(person, "Person to get overtime records for must be given.");

        return overtimeDAO.findByPerson(person);
    }


    @Override
    public Overtime record(Overtime overtime, Optional<String> comment, Person author) {

        boolean isNewOvertime = overtime.isNew();

        // save overtime record
        overtime.onUpdate();
        overtimeDAO.save(overtime);

        // save comment
        OvertimeAction action = isNewOvertime ? OvertimeAction.CREATED : OvertimeAction.EDITED;
        OvertimeComment overtimeComment = new OvertimeComment(author, overtime, action);
        comment.ifPresent((text) -> overtimeComment.setText(text));

        commentDAO.save(overtimeComment);

        String loggingAction = isNewOvertime ? "Created" : "Updated";
        LOG.info(loggingAction + " overtime record: " + overtime.toString());

        return overtime;
    }


    @Override
    public Optional<Overtime> getOvertimeById(Integer id) {

        Assert.notNull(id, "ID must be given.");

        return Optional.ofNullable(overtimeDAO.findOne(id));
    }


    @Override
    public List<OvertimeComment> getCommentsForOvertime(Overtime overtime) {

        Assert.notNull(overtime, "Overtime record to get comments for must be given.");

        return commentDAO.findByOvertime(overtime);
    }


    @Override
    public BigDecimal getTotalOvertimeForPerson(Person person) {

        Assert.notNull(person, "Person to get total overtime for must be given.");

        Optional<BigDecimal> totalOvertime = Optional.ofNullable(overtimeDAO.calculateTotalHoursForPerson(person));

        if (totalOvertime.isPresent()) {
            return totalOvertime.get();
        }

        return BigDecimal.ZERO;
    }
}
