package org.synyx.urlaubsverwaltung.web.jsp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("uv.template-engine.jsp")
public class JspProperties {

    private boolean usePrecompiled = false;

    public boolean usePrecompiled() {
        return usePrecompiled;
    }

    public void setUsePrecompiled(boolean usePrecompiled) {
        this.usePrecompiled = usePrecompiled;
    }
}
