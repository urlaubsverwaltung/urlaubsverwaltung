package org.synyx.urlaubsverwaltung.calendarintegration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import static jakarta.persistence.GenerationType.SEQUENCE;


@Entity(name = "calendar_integration_settings")
public class CalendarSettings extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "calendar_settings_generator")
    @SequenceGenerator(name = "calendar_settings_generator", sequenceName = "calendar_settings_id_seq", allocationSize = 1)
    private Long id;

    private GoogleCalendarSettings googleCalendarSettings = new GoogleCalendarSettings();

    @Column(name = "provider")
    private String provider;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GoogleCalendarSettings getGoogleCalendarSettings() {
        return googleCalendarSettings;
    }

    public void setGoogleCalendarSettings(GoogleCalendarSettings googleCalendarSettings) {
        this.googleCalendarSettings = googleCalendarSettings;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
