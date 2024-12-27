package org.synyx.urlaubsverwaltung.infobanner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class InfoBannerConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(InfoBannerConfiguration.class);

    @Test
    void ensureInfoBannerControllerAdviceExists() {
        contextRunner
            .withPropertyValues(
                "uv.info-banner.enabled=true",
                "uv.info-banner.text.de=Awesome Text"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(InfoBannerControllerAdvice.class);
            });
    }

    @Test
    void ensureInfoBannerControllerAdviceDoesNotExistWhenPropertyIsMissing() {
        contextRunner
            .run(context -> {
                assertThat(context).doesNotHaveBean(InfoBannerControllerAdvice.class);
            });
    }

    @Test
    void ensureInfoBannerControllerAdviceDoesNotExistWhenPropertyIsSetToDisabled() {
        contextRunner
            .withPropertyValues("uv.info-banner.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(InfoBannerControllerAdvice.class);
            });
    }
}
