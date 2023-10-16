package org.synyx.urlaubsverwaltung.settings;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings")
public class SettingsViewController implements HasLaunchpad {

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails() {
        return "redirect:/web/settings/absences";
    }
}
