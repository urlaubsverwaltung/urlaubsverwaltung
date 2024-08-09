package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteEntity;

interface SickNoteExtensionProjection {

    SickNoteEntity getSickNote();

    SickNoteExtensionEntity getSickNoteExtension();
}
