<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<spring:url var="URL_PREFIX" value="/web"/>

<form:form method="POST" id="remind" action="${URL_PREFIX}/application/${application.id}/remind"
           style="display:inline;"></form:form>

