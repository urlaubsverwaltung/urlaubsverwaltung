package org.synyx.urlaubsverwaltung.core.settings;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository for {@link Settings} entities.
 *
 * @author  Daniel Hammann - hammann@synyx.de
 */
public interface SettingsDAO extends JpaRepository<Settings, Integer> {
}
