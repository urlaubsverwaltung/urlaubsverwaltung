package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import static org.thymeleaf.templatemode.TemplateMode.TEXT;

@Configuration
class MailConfiguration {

    private static final String UTF_8 = "UTF-8";

    private final ApplicationContext applicationContext;

    @Autowired
    MailConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    ITemplateEngine emailTemplateEngine() {
        final SpringTemplateEngine emailTemplateEngine = new SpringTemplateEngine();
        emailTemplateEngine.addTemplateResolver(textTemplateResolver());
        emailTemplateEngine.setTemplateEngineMessageSource(emailMessageSource());
        return emailTemplateEngine;
    }

    @Bean
    ResourceBundleMessageSource emailMessageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding(UTF_8);
        messageSource.setBasename("MailMessages");
        return messageSource;
    }

    private SpringResourceTemplateResolver textTemplateResolver() {
        final SpringResourceTemplateResolver textEmailTemplateResolver = new SpringResourceTemplateResolver();
        textEmailTemplateResolver.setApplicationContext(applicationContext);
        textEmailTemplateResolver.setOrder(1);
        textEmailTemplateResolver.setPrefix("classpath:/mail/");
        textEmailTemplateResolver.setSuffix(".txt");
        textEmailTemplateResolver.setTemplateMode(TEXT);
        textEmailTemplateResolver.setCharacterEncoding(UTF_8);
        textEmailTemplateResolver.setCacheable(false);
        return textEmailTemplateResolver;
    }
}
