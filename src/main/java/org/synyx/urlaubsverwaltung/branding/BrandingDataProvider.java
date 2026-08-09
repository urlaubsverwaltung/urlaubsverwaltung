package org.synyx.urlaubsverwaltung.branding;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.web.DataProviderInterface;

/**
 * Provides the configured application name to every rendered view.
 */
@Component
class BrandingDataProvider implements DataProviderInterface {

    private final BrandingProperties brandingProperties;

    BrandingDataProvider(BrandingProperties brandingProperties) {
        this.brandingProperties = brandingProperties;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        if (addDataIf(modelAndView)) {
            modelAndView.addObject("applicationName", brandingProperties.name());
        }
    }
}
