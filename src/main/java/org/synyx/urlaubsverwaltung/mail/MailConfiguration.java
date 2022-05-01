package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

import static org.thymeleaf.templatemode.TemplateMode.TEXT;

@Configuration
class MailConfiguration {

    private static final String EMAIL_TEMPLATE_ENCODING = "UTF-8";

    private final ApplicationContext applicationContext;

    @Autowired
    MailConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    ITemplateEngine emailTemplateEngine() {
        final SpringTemplateEngine emailTemplateEngine = new SpringTemplateEngine();
        emailTemplateEngine.addTemplateResolver(textTemplateResolver());
        emailTemplateEngine.addDialect(new Java8TimeDialect());
        return emailTemplateEngine;
    }

    private SpringResourceTemplateResolver textTemplateResolver() {
        final SpringResourceTemplateResolver textEmailTemplateResolver = new SpringResourceTemplateResolver();
        textEmailTemplateResolver.setApplicationContext(applicationContext);
        textEmailTemplateResolver.setOrder(1);
        textEmailTemplateResolver.setPrefix("classpath:/mail/");
        textEmailTemplateResolver.setSuffix(".txt");
        textEmailTemplateResolver.setTemplateMode(TEXT);
        textEmailTemplateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
        textEmailTemplateResolver.setCacheable(false);
        return textEmailTemplateResolver;
    }
}
