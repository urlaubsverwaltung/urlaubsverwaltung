package org.synyx.urlaubsverwaltung.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.calendar.TimeSettings;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.person.settings.AvatarSettings;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.util.Objects;

import static jakarta.persistence.GenerationType.SEQUENCE;


/**
 * Represents the settings / business rules for the application.
 */
@Entity
public class Settings extends AbstractTenantAwareEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "settings_generator")
    @SequenceGenerator(name = "settings_generator", sequenceName = "settings_id_seq", allocationSize = 1)
    private Long id;

    @Embedded
    private ApplicationSettings applicationSettings = new ApplicationSettings();

    @Embedded
    private AccountSettings accountSettings = new AccountSettings();

    @Embedded
    private WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();

    @Embedded
    private OvertimeSettings overtimeSettings = new OvertimeSettings();

    @Embedded
    private TimeSettings timeSettings = new TimeSettings();

    @Embedded
    private SickNoteSettings sickNoteSettings = new SickNoteSettings();

    @Embedded
    private AvatarSettings avatarSettings = new AvatarSettings();

    @Embedded
    private PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicationSettings getApplicationSettings() {
        return applicationSettings;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void setAccountSettings(AccountSettings accountSettings) {
        this.accountSettings = accountSettings;
    }

    public WorkingTimeSettings getWorkingTimeSettings() {
        return workingTimeSettings;
    }

    public void setWorkingTimeSettings(WorkingTimeSettings workingTimeSettings) {
        this.workingTimeSettings = workingTimeSettings;
    }

    public OvertimeSettings getOvertimeSettings() {
        return overtimeSettings;
    }

    public void setOvertimeSettings(OvertimeSettings overtimeSettings) {
        this.overtimeSettings = overtimeSettings;
    }

    public TimeSettings getTimeSettings() {
        return timeSettings;
    }

    public void setTimeSettings(TimeSettings timeSettings) {
        this.timeSettings = timeSettings;
    }

    public SickNoteSettings getSickNoteSettings() {
        return sickNoteSettings;
    }

    public void setSickNoteSettings(SickNoteSettings sickNoteSettings) {
        this.sickNoteSettings = sickNoteSettings;
    }

    public AvatarSettings getAvatarSettings() {
        return avatarSettings;
    }

    public void setAvatarSettings(AvatarSettings avatarSettings) {
        this.avatarSettings = avatarSettings;
    }

    public PublicHolidaysSettings getPublicHolidaysSettings() {
        return publicHolidaysSettings;
    }

    public void setPublicHolidaysSettings(PublicHolidaysSettings publicHolidaysSettings) {
        this.publicHolidaysSettings = publicHolidaysSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Settings that = (Settings) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
