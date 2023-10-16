package org.synyx.urlaubsverwaltung.settings;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings/avatar")
public class SettingsAvatarViewController implements HasLaunchpad {

    private final SettingsService settingsService;

    @Autowired
    public SettingsAvatarViewController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsAvatarDto settingsDto = settingsToDto(settings);

        model.addAttribute("settings", settingsDto);

        return "settings/avatar/settings_avatar";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(@Valid @ModelAttribute("settings") SettingsAvatarDto settingsDto,
                                RedirectAttributes redirectAttributes) {

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsService.save(settings);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/avatar";
    }

    private SettingsAvatarDto settingsToDto(Settings settings) {
        // TODO use DTOs for settings
        final SettingsAvatarDto dto = new SettingsAvatarDto();
        dto.setId(settings.getId());
        dto.setAvatarSettings(settings.getAvatarSettings());
        return dto;
    }

    private Settings settingsDtoToSettings(SettingsAvatarDto dto) {
        final Settings settings = settingsService.getSettings();
        settings.setId(dto.getId());
        settings.setAvatarSettings(dto.getAvatarSettings());
        return settings;
    }
}
