package org.synyx.urlaubsverwaltung.core.sync.absence;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Optional;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
@Service
public class AbsenceMappingServiceImpl implements AbsenceMappingService {

    private final AbsenceMappingDAO absenceMappingDAO;

    @Autowired
    public AbsenceMappingServiceImpl(AbsenceMappingDAO absenceMappingDAO) {

        this.absenceMappingDAO = absenceMappingDAO;
    }

    @Override
    public AbsenceMapping create(Application application, String eventId) {

        AbsenceMapping absenceMapping = new AbsenceMapping(application.getId(), AbsenceType.VACATION, eventId);
        absenceMappingDAO.save(absenceMapping);

        return absenceMapping;
    }


    @Override
    public AbsenceMapping create(SickNote sickNote, String eventId) {

        AbsenceMapping absenceMapping = new AbsenceMapping(sickNote.getId(), AbsenceType.SICKNOTE, eventId);
        absenceMappingDAO.save(absenceMapping);

        return absenceMapping;
    }


    @Override
    public void delete(AbsenceMapping absenceMapping) {

        absenceMappingDAO.delete(absenceMapping);
    }


    @Override
    public Optional<AbsenceMapping> getAbsenceByIdAndType(Integer id, AbsenceType absenceType) {

        return Optional.ofNullable(absenceMappingDAO.findAbsenceMappingByAbsenceIdAndAbsenceType(id, absenceType));
    }
}
