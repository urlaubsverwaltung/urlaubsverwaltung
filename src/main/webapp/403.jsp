<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head/>
</head>

<body>

    <spring:url var="LOGIN" value="/login.jsp"/>

    <div class="error-container container-fluid">
        <div class="content">
            <div class="row">
                <div class="col-xs-12">
                    <h1 class="error-code">403</h1>
                </div>
            </div>
            <div class="row">
                <div class="col-xs-12">
                    <h2 class="error-description">
                        Dir fehlen die Berechtigungen für die angeforderte Seite.
                    </h2>
                    <p class="error-link">
                        <a href="${URL_PREFIX}/overview">Zurück zur Übersicht</a>
                    </p>
                </div>
            </div>
        </div>
    </div>

</body>

</html>
