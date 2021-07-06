package org.synyx.urlaubsverwaltung.sicknote.settings;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/sicknote/settings")
public class SickNoteSettingsController {

    private final SickNoteSettingsService sickNoteSettingsService;

    public SickNoteSettingsController(SickNoteSettingsService sickNoteSettingsService) {
        this.sickNoteSettingsService = sickNoteSettingsService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String getSickNoteSettings(Model model) {

        SickNoteSettingsDto settingsDto = sickNoteSettingsService.getSettingsDto();
        model.addAttribute("sickNoteSettings", settingsDto);

        return "sicknote/sicknote_settings";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String saveSickNoteSettings(@ModelAttribute("sickNoteSettings") SickNoteSettingsDto sickNoteSettingsDto,
                                       Model model) {

        sickNoteSettingsService.save(sickNoteSettingsDto);
        model.addAttribute("sickNoteSettings", sickNoteSettingsDto);
        model.addAttribute("success", true);

        return "sicknote/sicknote_settings";
    }
}
