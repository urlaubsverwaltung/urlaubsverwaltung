package org.synyx.urlaubsverwaltung.core.sicknote;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SickNoteTypeServiceImpl implements SickNoteTypeService {

    private SickNoteTypeDAO sickNoteTypeDAO;

    @Autowired
    public SickNoteTypeServiceImpl(SickNoteTypeDAO sickNoteTypeDAO) {

        this.sickNoteTypeDAO = sickNoteTypeDAO;
    }

    @Override
    public List<String> getSickNoteTypes() {

        List<String> result = new ArrayList<>();
        List<SickNoteType> sickNoteTypes = this.sickNoteTypeDAO.findAll();
        result.addAll(sickNoteTypes.stream().map(SickNoteType::getTypeName).collect(Collectors.toList()));

        return result;
    }
}
