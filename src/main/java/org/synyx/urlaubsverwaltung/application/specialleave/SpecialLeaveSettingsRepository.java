package org.synyx.urlaubsverwaltung.application.specialleave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface SpecialLeaveSettingsRepository extends JpaRepository<SpecialLeaveSettingsEntity, Long> {

    @Override
    List<SpecialLeaveSettingsEntity> findAll();

    @Override
    List<SpecialLeaveSettingsEntity> findAllById(Iterable<Long> ids);
}
