<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@attribute name="duration" type="java.time.Duration" required="true" %>

<%@ tag trimDirectiveWhitespaces="true" %>

<c:if test="${duration.negative}">-</c:if>
<fmt:formatNumber minIntegerDigits="2" value="${duration.abs().toHours()}"/>
<c:out value=":"/>
<fmt:formatNumber minIntegerDigits="2" value="${duration.abs().toMinutesPart()}"/>

