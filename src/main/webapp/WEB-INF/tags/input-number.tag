<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@attribute name="id" type="java.lang.String" required="true" %>
<%@attribute name="path" type="java.lang.String" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="true" %>
<%@attribute name="cssErrorClass" type="java.lang.String" required="true" %>
<%@attribute name="step" type="java.lang.Float" required="false" %>
<%@attribute name="placeholder" type="java.lang.String" required="false" %>
<%@attribute name="value" type="java.math.BigDecimal" required="true" %>

<spring:bind path="${path}">
    <input type="number"
           id="${path}"
           name="${path}"
           class="${status.error ? cssErrorClass : cssClass}"
           step="${not empty step ? step : 'any'}"
           value="${value}"
            <c:if test="${not empty placeholder}">
               placeholder="${placeholder}"
            </c:if>
    >
</spring:bind>
