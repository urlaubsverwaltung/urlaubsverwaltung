<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<%@attribute name="dateTime" type="org.joda.time.DateTime" required="true" %>

<c:set var="FORMAT_DATE_PATTERN">
    <spring:message code="pattern.date"/>
</c:set>

<joda:format pattern="${FORMAT_DATE_PATTERN}" value="${dateTime}" />