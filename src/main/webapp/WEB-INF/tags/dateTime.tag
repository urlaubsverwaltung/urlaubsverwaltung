<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@attribute name="dateTime" type="java.time.ZonedDateTime" required="true" %>

<c:set var="FORMAT_DATE_PATTERN">
    <spring:message code="pattern.date"/>
</c:set>

<fmt:parseDate value="${dateTime}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/>
<fmt:formatDate pattern="${FORMAT_DATE_PATTERN}" value="${parsedDate}" type="both" />
