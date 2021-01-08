<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="duration" type="java.time.Duration" required="true" %>

<c:if test="${duration.negative}">-</c:if>
<c:if test="${duration.abs().toHoursPart() > 0}">
    <c:out value="${duration.abs().toHoursPart()}"/><spring:message code="overtime.data.hours.abbr"/>
</c:if>
<c:if test="${duration.abs().toMinutesPart() > 0}">
    <c:out value="${duration.abs().toMinutesPart()}"/><spring:message code="overtime.data.minutes.abbr"/>
</c:if>

