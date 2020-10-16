<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="hours" type="java.math.BigDecimal" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>

<uv:overtime-total-box__ hours="${hours}" cssClass="tw-p-0 ${cssClass}" />
