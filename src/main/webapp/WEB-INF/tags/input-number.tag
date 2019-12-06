<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@attribute name="id" type="java.lang.String" required="true" %>
<%@attribute name="path" type="java.lang.String" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="true" %>
<%@attribute name="cssErrorClass" type="java.lang.String" required="true" %>
<%@attribute name="step" type="java.lang.Float" required="true" %>
<%@attribute name="value" type="java.math.BigDecimal" required="true" %>

<spring:bind path="${path}">
    <input name="${path}" class="${status.error ? cssErrorClass : cssClass}"
           step="${step}" type="number" value="${value}">
</spring:bind>
