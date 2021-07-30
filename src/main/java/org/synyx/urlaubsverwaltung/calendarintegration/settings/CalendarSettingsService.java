package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

import static org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsDtoMapper.mapToCalendarSettingsDto;
import static org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsDtoMapper.mapToCalendarSettingsEntity;

@Service
public class CalendarSettingsService {

    private final CalendarSettingsRepository calendarSettingsRepository;

    public CalendarSettingsService(CalendarSettingsRepository calendarSettingsRepository) {
        this.calendarSettingsRepository = calendarSettingsRepository;
    }

    public CalendarSettingsDto getSettingsDto(HttpServletRequest request) {
        final CalendarSettingsDto calendarSettingsDto = mapToCalendarSettingsDto(calendarSettingsRepository.findFirstBy());
        return initDto(calendarSettingsDto, request);
    }

    public CalendarSettingsDto save(HttpServletRequest request, CalendarSettingsDto calendarSettingsDto) {

        final CalendarSettingsDto refreshedCalendarSettingsDto = processGoogleRefreshToken(calendarSettingsDto);

        final CalendarSettingsEntity savedCalendarSettings =
            calendarSettingsRepository.save(mapToCalendarSettingsEntity(refreshedCalendarSettingsDto));

        final CalendarSettingsDto newCalendarSettingsDto = mapToCalendarSettingsDto(savedCalendarSettings);
        return initDto(newCalendarSettingsDto, request);
    }

    private CalendarSettingsDto processGoogleRefreshToken(CalendarSettingsDto calendarSettingsDto) {
        CalendarSettingsDto storedCalendarSettingsDto = mapToCalendarSettingsDto(calendarSettingsRepository.findFirstBy());

        final GoogleCalendarSettingsDto storedGoogleSettings = storedCalendarSettingsDto.getGoogleCalendarSettings();
        final GoogleCalendarSettingsDto updateGoogleSettings = calendarSettingsDto.getGoogleCalendarSettings();

        updateGoogleSettings.setRefreshToken(storedGoogleSettings.getRefreshToken());

        if (refreshTokenGotInvalid(storedGoogleSettings, updateGoogleSettings)) {
            // refresh token is invalid if settings changed
            updateGoogleSettings.setRefreshToken(null);
        }

        return calendarSettingsDto;
    }

    private boolean refreshTokenGotInvalid(GoogleCalendarSettingsDto oldSettings, GoogleCalendarSettingsDto newSettings) {
        if (oldSettings.getClientSecret() == null
            || oldSettings.getClientId() == null
            || oldSettings.getCalendarId() == null) {
            return true;
        }

        return !oldSettings.equals(newSettings);
    }

    private String getAuthorizedRedirectUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromContextPath(request).replacePath("/web/google-api-handshake").build().toString();
    }

    private CalendarSettingsDto initDto(CalendarSettingsDto calendarSettingsDto, HttpServletRequest request) {
        calendarSettingsDto.getGoogleCalendarSettings().setAuthorizedRedirectUrl(getAuthorizedRedirectUrl(request));
        return calendarSettingsDto;
    }

    public CalendarSettingsEntity getSettings() {
        return calendarSettingsRepository.findFirstBy();
    }

    public void save(CalendarSettingsEntity calendarSettingsEntity) {
        calendarSettingsRepository.save(calendarSettingsEntity);
    }
}
