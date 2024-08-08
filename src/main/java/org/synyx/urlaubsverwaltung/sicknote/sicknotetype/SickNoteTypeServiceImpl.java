package org.synyx.urlaubsverwaltung.sicknote.sicknotetype;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

@Service
class SickNoteTypeServiceImpl implements SickNoteTypeService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SickNoteTypeRepository sickNoteTypeRepository;

    @Autowired
    SickNoteTypeServiceImpl(SickNoteTypeRepository sickNoteTypeRepository) {
        this.sickNoteTypeRepository = sickNoteTypeRepository;
    }

    @Override
    public List<SickNoteType> getSickNoteTypes() {
        return this.sickNoteTypeRepository.findAll(Sort.by("id"));
    }


    @Override
    public void insertDefaultSickNoteTypes() {
        final long count = sickNoteTypeRepository.count();
        if (count == 0) {

            final SickNoteType sickNote = new SickNoteType();
            sickNote.setCategory(SICK_NOTE);
            sickNote.setMessageKey("application.data.sicknotetype.sicknote");

            final SickNoteType sickNoteChild = new SickNoteType();
            sickNoteChild.setCategory(SICK_NOTE_CHILD);
            sickNoteChild.setMessageKey("application.data.sicknotetype.sicknotechild");

            final List<SickNoteType> savesSickNoteTypes = sickNoteTypeRepository.saveAll(List.of(sickNote, sickNoteChild));
            LOG.info("Saved initial sick note types {}", savesSickNoteTypes);
        }
    }
}
