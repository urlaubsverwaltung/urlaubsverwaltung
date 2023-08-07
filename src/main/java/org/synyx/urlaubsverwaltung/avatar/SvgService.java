package org.synyx.urlaubsverwaltung.avatar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Locale;
import java.util.Map;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

@Service
class SvgService {

    private static final String RESOURCES_TEMPLATES_DIR_PREFIX = "templates/";

    private final ISpringTemplateEngine svgTemplateEngine;

    @Autowired
    SvgService(final MessageSource messageSource) {
        this.svgTemplateEngine = svgTemplateEngine(messageSource);
    }

    String createSvg(final String templateName, final Locale locale, final Map<String, Object> model) {
        return this.svgTemplateEngine.process(templateName, new Context(locale, model));
    }

    private ISpringTemplateEngine svgTemplateEngine(final MessageSource messageSource) {
        final SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(svgXmlTemplateResolver());
        engine.setMessageSource(messageSource);
        return engine;
    }

    private ITemplateResolver svgXmlTemplateResolver() {
        final ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(RESOURCES_TEMPLATES_DIR_PREFIX);
        resolver.setSuffix(".svg");
        resolver.setCacheable(false);
        resolver.setTemplateMode(HTML);
        return resolver;
    }
}
