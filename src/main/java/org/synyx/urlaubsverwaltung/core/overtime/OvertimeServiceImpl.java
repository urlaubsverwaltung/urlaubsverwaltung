package org.synyx.urlaubsverwaltung.core.overtime;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
@Service
public class OvertimeServiceImpl implements OvertimeService {

    private OvertimeDAO overtimeDAO;

    @Autowired
    public OvertimeServiceImpl(OvertimeDAO overtimeDAO) {

        this.overtimeDAO = overtimeDAO;
    }

    @Override
    public void save(Overtime overtime) {

        overtimeDAO.save(overtime);
    }
}
