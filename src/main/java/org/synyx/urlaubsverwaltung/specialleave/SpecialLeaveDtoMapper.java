package org.synyx.urlaubsverwaltung.specialleave;

import java.util.ArrayList;
import java.util.List;

public final class SpecialLeaveDtoMapper {

    private SpecialLeaveDtoMapper() {
    }

    public static List<SpecialLeaveEntity> mapToSpecialLeaveEntities(SpecialLeaveSettingsDto specialLeaveSettingsDto) {
        List<SpecialLeaveEntity> specialLeaveEntities = new ArrayList<>();

        SpecialLeaveEntity birthOfChildSpecialLeave = toSpecialLeaveEntity(specialLeaveSettingsDto.getBirthOfCild(),
            specialLeaveSettingsDto.getBirthOfChildDays());
        specialLeaveEntities.add(birthOfChildSpecialLeave);

        SpecialLeaveEntity deathOfSpuseOrChildSpecialLeave = toSpecialLeaveEntity(specialLeaveSettingsDto.getDeathOfSpuseOrChild(),
            specialLeaveSettingsDto.getDeathOfSpuseOrChildDays());
        specialLeaveEntities.add(deathOfSpuseOrChildSpecialLeave);

        SpecialLeaveEntity ownWeddingSpecialLeave = toSpecialLeaveEntity(specialLeaveSettingsDto.getOwnWedding(),
            specialLeaveSettingsDto.getOwnWeddingDays());
        specialLeaveEntities.add(ownWeddingSpecialLeave);

        SpecialLeaveEntity deathOfParentSpecialLeave = toSpecialLeaveEntity(specialLeaveSettingsDto.getDeathOfParent(),
            specialLeaveSettingsDto.getDeathOfParentDays());
        specialLeaveEntities.add(deathOfParentSpecialLeave);

        SpecialLeaveEntity relocationSpecialLeave = toSpecialLeaveEntity(specialLeaveSettingsDto.getRelocationForOperationalReasons(),
            specialLeaveSettingsDto.getRelocationForOperationalReasonsDays());
        specialLeaveEntities.add(relocationSpecialLeave);

        return specialLeaveEntities;
    }

    private static SpecialLeaveEntity toSpecialLeaveEntity(SpecialLeave birthOfCild, int birthOfChildDays) {
        SpecialLeaveEntity specialLeaveEntity = new SpecialLeaveEntity();
        specialLeaveEntity.setSpecialLeave(birthOfCild);
        specialLeaveEntity.setDays(birthOfChildDays);
        return specialLeaveEntity;
    }

    public static SpecialLeaveSettingsDto mapToSpecialLeaveDto(List<SpecialLeaveEntity> specialLeaveEntiies) {

        SpecialLeaveSettingsDto specialLeaveSettingsDto = new SpecialLeaveSettingsDto();

        specialLeaveEntiies.forEach(specialLeaveEntity -> {
            switch (specialLeaveEntity.getSpecialLeave()) {
                    case OWN_WEDDING:
                        specialLeaveSettingsDto.setOwnWedding(specialLeaveEntity.getSpecialLeave());
                        specialLeaveSettingsDto.setOwnWeddingDays(specialLeaveEntity.getDays());
                        break;
                    case DEATH_OF_PARENT:
                        specialLeaveSettingsDto.setDeathOfParent(specialLeaveEntity.getSpecialLeave());
                        specialLeaveSettingsDto.setDeathOfParentDays(specialLeaveEntity.getDays());
                        break;
                    case BIRTH_OF_A_CHILD:
                        specialLeaveSettingsDto.setBirthOfCild(specialLeaveEntity.getSpecialLeave());
                        specialLeaveSettingsDto.setBirthOfChildDays(specialLeaveEntity.getDays());
                        break;
                    case RELOCATION_FOR_OPERATIONAL_REASONS:
                        specialLeaveSettingsDto.setRelocationForOperationalReasons(specialLeaveEntity.getSpecialLeave());
                        specialLeaveSettingsDto.setRelocationForOperationalReasonsDays(specialLeaveEntity.getDays());
                        break;
                    case DEATH_OF_SPOUSE_OR_CHILD:
                        specialLeaveSettingsDto.setDeathOfSpuseOrChild(specialLeaveEntity.getSpecialLeave());
                        specialLeaveSettingsDto.setDeathOfSpuseOrChildDays(specialLeaveEntity.getDays());
                        break;
                    default:
                        break;
                }
        });

        return specialLeaveSettingsDto;
    }
}
