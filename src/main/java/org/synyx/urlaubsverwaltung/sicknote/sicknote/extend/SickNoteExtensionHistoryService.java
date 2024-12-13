package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;


import java.util.List;

public interface SickNoteExtensionHistoryService {
    List<SickNoteExtensionHistory> getSickNoteExtensionHistory(Long sickNoteId);
}
