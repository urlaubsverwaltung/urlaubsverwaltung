package org.synyx.urlaubsverwaltung.application.specialleave;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface SpecialLeaveSettingsRepository extends CrudRepository<SpecialLeaveSettingsEntity, Integer> {

    @Override
    List<SpecialLeaveSettingsEntity> findAll();

    @Override
    List<SpecialLeaveSettingsEntity> findAllById(Iterable<Integer> ids);
}
