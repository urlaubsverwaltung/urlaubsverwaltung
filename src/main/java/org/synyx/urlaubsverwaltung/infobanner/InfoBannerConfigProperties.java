package org.synyx.urlaubsverwaltung.infobanner;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "uv.info-banner")
public class InfoBannerConfigProperties {

    private boolean enabled;

    @NotNull
    @Valid
    private Text text;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public static class Text {
        @NotEmpty
        private String de;

        public String getDe() {
            return de;
        }

        public void setDe(String de) {
            this.de = de;
        }
    }
}
