package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synyx.urlaubsverwaltung.domain.Antrag;

/**
 * @author johannes
 */
public interface AntragDAO extends JpaRepository<Antrag, Integer> {

	// empty - for now...
}
