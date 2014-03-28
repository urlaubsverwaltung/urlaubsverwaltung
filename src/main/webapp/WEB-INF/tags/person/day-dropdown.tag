<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%@attribute name="path" type="java.lang.String" required="true" %>
<%@attribute name="selected" type="java.lang.String" required="true" %>

<form:select path="${path}" size="1">
    <c:forEach begin="1" end="31" step="1" var="day">
        <c:choose>
            <c:when test="${selected == day}">
                <form:option value="${day}" selected="selected">${day}</form:option>
            </c:when>
            <c:otherwise>
                <form:option value="${day}">${day}</form:option>
            </c:otherwise>
        </c:choose>
    </c:forEach>
</form:select>