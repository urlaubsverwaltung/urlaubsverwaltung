package org.synyx.urlaubsverwaltung.sicknote.settings;

import org.springframework.stereotype.Service;

@Service
public class SickNoteSettingsService {

    private final SickNoteSettingsRepository sickNoteRepository;

    public SickNoteSettingsService(SickNoteSettingsRepository sickNoteRepository) {
        this.sickNoteRepository = sickNoteRepository;
    }

    public SickNoteSettingsDto getSettingsDto() {
        SickNoteSettingsEntity sickNoteSettingsEntity = sickNoteRepository.findFirstBy();
        return SickNoteSettingsDtoMapper.mapToSickNoteSettingsDto(sickNoteSettingsEntity);
    }

    public void save(SickNoteSettingsDto sickNoteSettingsDto) {

        SickNoteSettingsEntity sickNoteSettingsEntity =
            SickNoteSettingsDtoMapper.mapToSickNoteSettingsEntity(sickNoteSettingsDto);

        sickNoteRepository.save(sickNoteSettingsEntity);
    }

    public SickNoteSettingsEntity getSettings() {
        return sickNoteRepository.findFirstBy();
    }
}
