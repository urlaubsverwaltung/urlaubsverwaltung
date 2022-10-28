package org.synyx.urlaubsverwaltung.web.jsp.config;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JspServletRegistratorTest {

    @Test
    void happyPath() throws FileNotFoundException {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        InputStream is = new FileInputStream(Paths.get("src/test/resources/web.xml").toFile());

        when(servletContext.getResourceAsStream("web.xml")).thenReturn(is);
        ServletRegistration.Dynamic value = Mockito.mock(ServletRegistration.Dynamic.class);
        when(servletContext.addServlet(anyString(), anyString())).thenReturn(value);

        ServletRegistration servletRegistration = Mockito.mock(ServletRegistration.class);
        when(servletContext.getServletRegistration(anyString())).thenReturn(servletRegistration);

        JspServletRegistrator jspServletRegistrator = new JspServletRegistrator("web.xml");
        jspServletRegistrator.onStartup(servletContext);

        verify(servletContext).getResourceAsStream("web.xml");
        verify(servletContext).addServlet("org.apache.jsp.WEB_002dINF.jsp.application.include.app_002ddetail_002delements.actions.reject_005fform_jsp", "org.apache.jsp.WEB_002dINF.jsp.application.include.app_002ddetail_002delements.actions.reject_005fform_jsp");
        verify(value).setLoadOnStartup(99);

        verify(servletContext).getServletRegistration("org.apache.jsp.WEB_002dINF.jsp.application.include.app_002ddetail_002delements.actions.reject_005fform_jsp");
    }
}
