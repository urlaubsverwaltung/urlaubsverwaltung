package org.synyx.urlaubsverwaltung.core.overtime;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Optional;

import javax.transaction.Transactional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
@Service
@Transactional
public class OvertimeInteractionServiceImpl implements OvertimeInteractionService {

    private static final Logger LOG = Logger.getLogger(OvertimeInteractionServiceImpl.class);

    private OvertimeService overtimeService;
    private OvertimeCommentService overtimeCommentService;

    @Autowired
    public OvertimeInteractionServiceImpl(OvertimeService overtimeService,
        OvertimeCommentService overtimeCommentService) {

        this.overtimeService = overtimeService;
        this.overtimeCommentService = overtimeCommentService;
    }

    @Override
    public Overtime record(Overtime overtime, Optional<String> comment, Person recorder) {

        overtimeService.save(overtime);
        overtimeCommentService.create(overtime, OvertimeAction.CREATED, comment, recorder);

        LOG.info("Created overtime record: " + overtime.toString());

        return overtime;
    }
}
