<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>


<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="errorPage.header.title"/>
    </title>
    <uv:custom-head/>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="tw-text-center tw-mt-24">
    <h1 class="error-code" title="${exception.message}">
        <c:choose>
            <c:when test="${statusCode == 403 || statusCode == 404}">
                <c:out value="${statusCode}"/>
            </c:when>
            <c:otherwise>
                <icon:emoji-sad className="tw-w-40 tw-h-40" />
            </c:otherwise>
        </c:choose>
    </h1>
    <p class="error-description">
        <c:choose>
            <c:when test="${statusCode == 403}">
                <spring:message code="errorPage.403"/>
            </c:when>
            <c:when test="${statusCode == 404}">
                <spring:message code="errorPage.404"/>
            </c:when>
            <c:otherwise>
                <spring:message code="errorPage.other"/>
            </c:otherwise>
        </c:choose>
    </p>
    <p class="error-link">
        <a href="${URL_PREFIX}/overview"><spring:message code="errorPage.back"/></a>
    </p>
</div>

</body>

</html>
