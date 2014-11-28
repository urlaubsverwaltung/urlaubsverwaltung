<%@page contentType="text/html" pageEncoding="UTF-8" %>
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

    <spring:url var="URL_PREFIX" value="/web"/>

    <div class="error-container container-fluid">
        <div class="content">
            <div class="row">
                <div class="col-xs-12">
                    <h1 class="error-code">404</h1>
                </div>
            </div>
            <div class="row">
                <div class="col-xs-12">
                    <h2 class="error-description">
                        Die angeforderte Seite konnte nicht gefunden werden.
                    </h2>
                </div>
            </div>
            <div class="row">
                <div class="col-xs-12">
                    <a href="${URL_PREFIX}/overview" class="error-btn btn btn-primary btn-lg col-xs-12 col-sm-8 col-sm-offset-2 col-md-4 col-md-offset-4"><i class="fa fa-home"></i> Zurück zur Übersicht</a>
                </div>
            </div>
        </div>
    </div>

</body>

</html>
