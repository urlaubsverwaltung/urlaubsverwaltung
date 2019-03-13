package org.synyx.urlaubsverwaltung.core.settings;

import org.springframework.data.repository.CrudRepository;


/**
 * Repository for {@link Settings} entities.
 *
 * @author  Daniel Hammann - hammann@synyx.de
 */
public interface SettingsDAO extends CrudRepository<Settings, Integer> {
}
