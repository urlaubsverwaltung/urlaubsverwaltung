package org.synyx.urlaubsverwaltung.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("uv.security.oauth2")
public class Oauth2LoginConfigurationProperties {

    /**
     * redirect to a login page url
     */
    private String loginPageUrl;

    public String getLoginPageUrl() {
        return loginPageUrl;
    }

    public void setLoginPageUrl(String loginPageUrl) {
        this.loginPageUrl = loginPageUrl;
    }
}
