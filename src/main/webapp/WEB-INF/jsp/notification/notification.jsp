<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<%@ page trimDirectiveWhitespaces="true" %>
<spring:url var="URL_PREFIX" value="/web"/>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="notification.header.title"/>
    </title>
    <uv:custom-head/>
</head>

<body>

<uv:menu/>

<div class="content">
    <div class="container">
        <h1>
            <spring:message code="notification.title"/>
        </h1>
        <p>Alles erledigt. Genieße deinen Tag!</p>
    </div>
</div>

</body>
