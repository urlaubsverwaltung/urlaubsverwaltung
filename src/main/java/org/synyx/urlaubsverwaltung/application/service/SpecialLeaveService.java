package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.dao.SpecialLeaveRepository;
import org.synyx.urlaubsverwaltung.application.domain.SpecialLeaveDing;

import java.util.List;

@Service
public class SpecialLeaveService {

    private final SpecialLeaveRepository specialLeaveRepository;

    @Autowired
    public SpecialLeaveService(SpecialLeaveRepository specialLeaveRepository) {
        this.specialLeaveRepository = specialLeaveRepository;
    }

    // TODO Mapper
    public List<SpecialLeaveDing> getSpecialLeaves() {
        return specialLeaveRepository.findAll();
    }


}
