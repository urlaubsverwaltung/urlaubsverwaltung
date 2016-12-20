package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.commons.lang.CharEncoding;

import org.apache.velocity.app.VelocityEngine;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.ui.velocity.VelocityEngineUtils;

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

        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, getFullyQualifiedTemplateName(templateName),
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
}
