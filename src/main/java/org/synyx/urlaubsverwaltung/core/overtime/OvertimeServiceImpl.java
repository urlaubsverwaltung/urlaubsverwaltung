package org.synyx.urlaubsverwaltung.core.overtime;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;

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

        return overtimeDAO.findByPerson(person);
    }


    @Override
    public Overtime record(Overtime overtime, Optional<String> comment, Person author) {

        // save overtime record
        overtimeDAO.save(overtime);

        // save comment
        OvertimeComment overtimeComment = new OvertimeComment(author, overtime, OvertimeAction.CREATED);
        comment.ifPresent((text) -> overtimeComment.setText(text));

        commentDAO.save(overtimeComment);

        LOG.info("Created overtime record: " + overtime.toString());

        return overtime;
    }
}
