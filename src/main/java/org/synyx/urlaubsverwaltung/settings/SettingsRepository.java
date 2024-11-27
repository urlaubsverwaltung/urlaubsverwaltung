package org.synyx.urlaubsverwaltung.settings;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Repository for {@link Settings} entities.
 */
public interface SettingsRepository extends CrudRepository<Settings, Long> {
    List<Settings> findAll();
}
