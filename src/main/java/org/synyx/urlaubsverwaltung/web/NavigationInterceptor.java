package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
class NavigationInterceptor implements HandlerInterceptor {

    private final SettingsService settingsService;

    @Autowired
    NavigationInterceptor(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            modelAndView.addObject("navigationOvertimeItemEnabled", overtimeEnabled());
        }
    }

    private boolean overtimeEnabled() {
        final OvertimeSettings overtimeSettings = settingsService.getSettings().getOvertimeSettings();
        return overtimeSettings.isOvertimeActive();
    }
}
