<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@attribute name="dateTime" type="java.time.ZonedDateTime" required="true" %>

<c:set var="FORMAT_TIME_PATTERN">
    <spring:message code="pattern.time"/>
</c:set>

<fmt:parseDate pattern="yyyy-MM-dd'T'HH:mm" value="${dateTime}" var="parsedDateTime" type="time"/>
<fmt:formatDate pattern="${FORMAT_TIME_PATTERN}" value="${parsedDateTime}" type="time"/>
