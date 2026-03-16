package org.synyx.urlaubsverwaltung.settings;

import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Repository for {@link Settings} entities.
 */
public interface SettingsRepository extends CrudRepository<Settings, Long> {

    @Override
    @NonNull List<Settings> findAll();
}
