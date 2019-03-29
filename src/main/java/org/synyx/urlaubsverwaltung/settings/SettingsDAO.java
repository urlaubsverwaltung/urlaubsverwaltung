package org.synyx.urlaubsverwaltung.settings;

import org.springframework.data.repository.CrudRepository;


/**
 * Repository for {@link Settings} entities.
 */
public interface SettingsDAO extends CrudRepository<Settings, Integer> {
}
