package org.synyx.urlaubsverwaltung.mail;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;


/**
 * Builds mail content by filling templates with data.
 */
@Service
class MailContentBuilder {

    private static final String FILE_EXTENSION = ".ftl";

    private final Configuration freemarkerConfiguration;

    @Autowired
    MailContentBuilder(Configuration freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    /**
     * Build text that can be set as mail body using the given model to fill the template with the given name.
     *
     * @param templateName of the template to be used
     * @param model        to fill the template
     * @param locale       the locale used for the email template
     * @return the text representation of the filled template
     */
    String buildMailBody(String templateName, Map<String, Object> model, Locale locale) {

        final String templateFilename = templateName + FILE_EXTENSION;

        try {
            final Template template = freemarkerConfiguration.getTemplate(templateFilename, locale);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (TemplateException | IOException e) {
            throw new MailContentBuilderException("Something went wrong processing email template=" + templateName, e);
        }
    }
}
