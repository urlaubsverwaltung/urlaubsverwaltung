package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.data.jpa.repository.JpaRepository;


interface SickNoteTypeDAO extends JpaRepository<SickNoteType, Integer> {
}
