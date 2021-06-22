package org.synyx.urlaubsverwaltung.sicknote.settings;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SickNoteSettingsDtoMapper {

    public static SickNoteSettingsDto mapToSickNoteSettingsDto(SickNoteSettingsEntity sickNoteSettingsEntity) {

        return new ObjectMapper().convertValue(sickNoteSettingsEntity, SickNoteSettingsDto.class);
    }

    public static SickNoteSettingsEntity mapToSickNoteSettingsEntity(SickNoteSettingsDto sickNoteSettingsDto) {

        return new ObjectMapper().convertValue(sickNoteSettingsDto, SickNoteSettingsEntity.class);
    }
}
