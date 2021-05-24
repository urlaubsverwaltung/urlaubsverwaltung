<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@attribute name="date" type="java.time.LocalDate" required="true" %>
<%@attribute name="pattern" type="java.lang.String" required="false" %>

<c:choose>
    <c:when test="${not empty pattern}">
        <c:set var="FORMAT_DATE_TIME_PATTERN">
            ${pattern}
        </c:set>
    </c:when>
    <c:otherwise>
        <c:set var="FORMAT_DATE_TIME_PATTERN">
            <spring:message code="pattern.date"/>
        </c:set>
    </c:otherwise>
</c:choose>

<fmt:parseDate value="${date}" pattern="yyyy-MM-dd" var="parsedDate" type="date"/>

<fmt:formatDate pattern="${FORMAT_DATE_TIME_PATTERN}" value="${parsedDate}" type="date" />
