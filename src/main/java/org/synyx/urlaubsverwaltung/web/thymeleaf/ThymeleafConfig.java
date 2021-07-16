package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

@Configuration
class ThymeleafConfig {

    @Bean
    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver templateResolver, AssetFilenameHashMapper assetFilenameHashMapper) {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setEnableSpringELCompiler(true);
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.addDialect(new AssetDialect(assetFilenameHashMapper));
        return templateEngine;
    }
}
