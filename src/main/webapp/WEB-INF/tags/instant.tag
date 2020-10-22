<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@attribute name="date" type="java.time.Instant" required="true" %>

<c:set var="FORMAT_DATE_TIME_PATTERN">
    <spring:message code="pattern.date"/>
</c:set>

<fmt:parseDate value="${date}" pattern="yyyy-MM-dd" var="parsedDate" type="date"/>

<fmt:formatDate pattern="${FORMAT_DATE_TIME_PATTERN}" value="${parsedDate}" type="date" />
