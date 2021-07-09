package org.synyx.urlaubsverwaltung.specialleave;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SpecialLeaveRepository extends CrudRepository<SpecialLeaveEntity, Integer> {

    @Modifying
    @Query("update special_leave_settings s set s.days = ?1 where s.specialLeave = ?2")
    void updateDaysOfSpecialLeave(int days, SpecialLeave specialLeave);
}
