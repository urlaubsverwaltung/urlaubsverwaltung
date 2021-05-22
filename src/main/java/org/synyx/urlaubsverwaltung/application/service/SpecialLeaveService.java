package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.dao.SpecialLeaveRepository;
import org.synyx.urlaubsverwaltung.application.domain.SpecialLeaveDing;
import org.synyx.urlaubsverwaltung.application.domain.SpecialLeaveDingMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Service
public class SpecialLeaveService {

    private final SpecialLeaveRepository specialLeaveRepository;

    @Autowired
    public SpecialLeaveService(SpecialLeaveRepository specialLeaveRepository) {
        this.specialLeaveRepository = specialLeaveRepository;
    }

    // TODO Mapper
    public List<SpecialLeaveDing> getSpecialLeaves() {

        return StreamSupport.stream(specialLeaveRepository.findAll().spliterator(), false)
            .map(SpecialLeaveDingMapper::mapToSpecialLeaveDing)
            .collect(toList());
    }


}
