package org.synyx.urlaubsverwaltung.web.jsp.config;

import org.apache.tomcat.util.descriptor.web.ServletDef;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;
import org.slf4j.Logger;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.xml.sax.InputSource;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

public class JspServletRegistrator implements ServletContextInitializer {

    private static final Logger LOG = getLogger(lookup().lookupClass());
    private static final int LOAD_ON_STARTUP = 99;

    private final String webXmlPath;

    public JspServletRegistrator(String webXmlPath) {
        this.webXmlPath = webXmlPath;
    }

    @Override
    public void onStartup(ServletContext servletContext) {
        final WebXml webXml = parseWebXml(servletContext);
        webXml.getServlets().forEach(registerServlet(servletContext));
        webXml.getServletMappings().forEach(addUrlMappingToServlet(servletContext));
    }

    private WebXml parseWebXml(ServletContext servletContext) {

        final WebXml webXml = new WebXml();
        try (InputStream inputStream = servletContext.getResourceAsStream(webXmlPath)) {
            if (inputStream == null) {
                throw new IllegalStateException(String.format("Could not read %s!", webXmlPath));
            }

            final WebXmlParser parser = new WebXmlParser(false, false, true);
            final boolean success = parser.parseWebXml(new InputSource(inputStream), webXml, false);
            if (!success) {
                throw new IllegalStateException("Something went wrong registering precompiled JSPs");
            }

            LOG.info("Parsed {} including {} servlets and {} servletMappings!", webXmlPath, webXml.getServlets().size(), webXml.getServletMappings().size());
        } catch (IOException e) {
            throw new IllegalStateException("Could not close the web.xml input stream to precompile jsps");
        }

        return webXml;
    }

    private BiConsumer<String, ServletDef> registerServlet(ServletContext servletContext) {
        return (key, servletDef) -> {
            final String servletName = servletDef.getServletName();
            final String servletClass = servletDef.getServletClass();

            LOG.debug("Registering precompiled JSP: servletName={} -> servletClass={}", servletName, servletClass);
            final ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(servletName, servletClass);
            servletRegistration.setLoadOnStartup(LOAD_ON_STARTUP);
        };
    }

    private BiConsumer<String, String> addUrlMappingToServlet(ServletContext servletContext) {
        return (urlPattern, servletClass) -> {
            LOG.debug("Mapping servlet: urlPattern={} -> servletClass={}", urlPattern, servletClass);
            servletContext.getServletRegistration(servletClass).addMapping(urlPattern);
        };
    }
}
