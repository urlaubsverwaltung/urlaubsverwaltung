<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="from" type="java.time.LocalDate" required="true" %>
<%@attribute name="to" type="java.time.LocalDate" required="true" %>
<%@attribute name="pattern" type="java.lang.String" required="false" %>

<c:set var="START_DATE">
    <uv:date date="${from}" pattern="${pattern}" />
</c:set>
<c:set var="END_DATE">
    <uv:date date="${to}" pattern="${pattern}" />
</c:set>

<spring:message code="date.range" arguments="${START_DATE};${END_DATE}" argumentSeparator=";"/>
