package org.synyx.urlaubsverwaltung.web.headerscript;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderScriptConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(HeaderScriptConfiguration.class);

    @Test
    void ensureHeaderScriptControllerAdviceExists() {
        contextRunner
            .withPropertyValues(
                "uv.header-script.enabled=true",
                "uv.header-script.content=<script src=\"test.js\"></script>"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(HeaderScriptControllerAdvice.class);
            });
    }

    @Test
    void ensureHeaderScriptControllerAdviceExistsWithEmptyContent() {
        contextRunner
            .withPropertyValues("uv.header-script.enabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(HeaderScriptControllerAdvice.class);
            });
    }

    @Test
    void ensureHeaderScriptControllerAdviceDoesNotExistWhenPropertyIsMissing() {
        contextRunner
            .run(context -> {
                assertThat(context).doesNotHaveBean(HeaderScriptControllerAdvice.class);
            });
    }

    @Test
    void ensureHeaderScriptControllerAdviceDoesNotExistWhenPropertyIsSetToDisabled() {
        contextRunner
            .withPropertyValues("uv.header-script.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(HeaderScriptControllerAdvice.class);
            });
    }
}
