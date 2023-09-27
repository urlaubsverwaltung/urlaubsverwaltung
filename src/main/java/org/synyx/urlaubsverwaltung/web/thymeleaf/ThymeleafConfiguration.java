package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.synyx.urlaubsverwaltung.web.AssetManifestService;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
@ConditionalOnClass({TemplateMode.class, SpringTemplateEngine.class})
class ThymeleafConfiguration implements WebMvcConfigurer {

    private MessageSource messageSource;

    @Autowired
    void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addPrinter(new DurationPrinter(messageSource));
    }

    @Bean
    AssetDialect assetDialect(AssetManifestService assetManifestService) {
        return new AssetDialect(assetManifestService);
    }
}
