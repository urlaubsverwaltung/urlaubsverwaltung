package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.commons.lang.CharEncoding;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import org.apache.velocity.exception.VelocityException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;


/**
 * Builds mail content by filling Velocity templates with data.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
class MailBuilder {

    private static final String TEMPLATE_PATH = "/org/synyx/urlaubsverwaltung/core/mail/";
    private static final String TEMPLATE_TYPE = ".vm";

    private final VelocityEngine velocityEngine;

    @Autowired
    MailBuilder(VelocityEngine velocityEngine) {

        this.velocityEngine = velocityEngine;
    }

    /**
     * Build text that can be set as mail body using the given model to fill the template with the given name.
     *
     * @param  templateName  of the template to be used
     * @param  model  to fill the template
     *
     * @return  the text representation of the filled template
     */
    String buildMailBody(String templateName, Map<String, Object> model) {

        return mergeTemplateIntoString(velocityEngine, getFullyQualifiedTemplateName(templateName),
                CharEncoding.UTF_8, model);
    }


    /**
     * Get fully qualified template name including path and file extension of the given template name.
     *
     * @param  templateName  to get the fully qualified template name of
     *
     * @return  the fully qualified template name using {@value #TEMPLATE_PATH} as path and {@value #TEMPLATE_TYPE} as
     *          file extension
     */
    private String getFullyQualifiedTemplateName(String templateName) {

        return TEMPLATE_PATH + templateName + TEMPLATE_TYPE;
    }


    //
    // Code below copied from Spring's VelocityEngineUtils (because they are deprecated in Spring 4.3
    // and will be removed in future versions)
    //


    /**
     * Merge the specified Velocity template with the given model into a String.
     * <p>When using this method to prepare a text for a mail to be sent with Spring's
     * mail support, consider wrapping VelocityException in MailPreparationException.
     * @param velocityEngine VelocityEngine to work with
     * @param templateLocation the location of template, relative to Velocity's resource loader path
     * @param encoding the encoding of the template file
     * @param model the Map that contains model names as keys and model objects as values
     * @return the result as String
     * @throws VelocityException if the template wasn't found or rendering failed
     * @see org.springframework.mail.MailPreparationException
     */
    private static String mergeTemplateIntoString(VelocityEngine velocityEngine, String templateLocation,
                                                 String encoding, Map<String, Object> model) throws VelocityException {

        StringWriter result = new StringWriter();
        mergeTemplate(velocityEngine, templateLocation, encoding, model, result);
        return result.toString();
    }

    /**
     * Merge the specified Velocity template with the given model and write the result
     * to the given Writer.
     * @param velocityEngine VelocityEngine to work with
     * @param templateLocation the location of template, relative to Velocity's resource loader path
     * @param encoding the encoding of the template file
     * @param model the Map that contains model names as keys and model objects as values
     * @param writer the Writer to write the result to
     * @throws VelocityException if the template wasn't found or rendering failed
     */
    private static void mergeTemplate(
            VelocityEngine velocityEngine, String templateLocation, String encoding,
            Map<String, Object> model, Writer writer) throws VelocityException {

        VelocityContext velocityContext = new VelocityContext(model);
        velocityEngine.mergeTemplate(templateLocation, encoding, velocityContext, writer);
    }
}
