package org.synyx.urlaubsverwaltung.core.settings;

import org.springframework.data.repository.CrudRepository;


/**
 * Repository for {@link Settings} entities.
 */
public interface SettingsDAO extends CrudRepository<Settings, Integer> {
}
