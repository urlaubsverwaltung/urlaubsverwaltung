<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="path" type="java.lang.String" required="true" %>
<%@attribute name="selected" type="java.lang.String" required="true" %>

<form:select class="input-medium" path="${path}" size="1">
    <c:forEach begin="1" end="12" step="1" var="month">
        <c:choose>
            <c:when test="${selected == month}">
                <form:option value="${month}" selected="selected"><spring:message code="month.${month}" /></form:option>
            </c:when>
            <c:otherwise>
                <form:option value="${month}"><spring:message code="month.${month}" /></form:option>
            </c:otherwise>
        </c:choose>
    </c:forEach>
</form:select>