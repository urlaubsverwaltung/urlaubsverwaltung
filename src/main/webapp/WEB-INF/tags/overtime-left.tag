<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="hours" type="java.time.Duration" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>

<uv:overtime-left-box__ hours="${hours}" cssClass="tw-p-5 ${cssClass}"/>
