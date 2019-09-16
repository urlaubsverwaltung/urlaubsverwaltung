<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1"/>
    <title><spring:message code="login.title"/></title>
    <link rel="apple-touch-icon" sizes="57x57" href="<spring:url value='/favicons/apple-icon-57x57.png' />"/>
    <link rel="apple-touch-icon" sizes="60x60" href="<spring:url value='/favicons/apple-icon-60x60.png' />"/>
    <link rel="apple-touch-icon" sizes="72x72" href="<spring:url value='/favicons/apple-icon-72x72.png' />"/>
    <link rel="apple-touch-icon" sizes="76x76" href="<spring:url value='/favicons/apple-icon-76x76.png' />"/>
    <link rel="apple-touch-icon" sizes="114x114" href="<spring:url value='/favicons/apple-icon-114x114.png' />"/>
    <link rel="apple-touch-icon" sizes="120x120" href="<spring:url value='/favicons/apple-icon-120x120.png' />"/>
    <link rel="apple-touch-icon" sizes="144x144" href="<spring:url value='/favicons/apple-icon-144x144.png' />"/>
    <link rel="apple-touch-icon" sizes="152x152" href="<spring:url value='/favicons/apple-icon-152x152.png' />"/>
    <link rel="apple-touch-icon" sizes="180x180" href="<spring:url value='/favicons/apple-icon-180x180.png' />"/>
    <link rel="icon" type="image/png" sizes="192x192"  href="<spring:url value='/favicons/android-icon-192x192.png' />"/>
    <link rel="icon" type="image/png" sizes="32x32" href="<spring:url value='/favicons/favicon-32x32.png' />"/>
    <link rel="icon" type="image/png" sizes="96x96" href="<spring:url value='/favicons/favicon-96x96.png' />"/>
    <link rel="icon" type="image/png" sizes="16x16" href="<spring:url value='/favicons/favicon-16x16.png' />"/>
    <link rel="manifest" href="<spring:url value='/manifest.json' />"/>
    <meta name="msapplication-TileColor" content="#ffffff">
    <meta name="msapplication-TileImage" content="<spring:url value='/favicons/ms-icon-144x144.png"' />"/>
    <meta name="theme-color" content="#ffffff">
    <link rel="stylesheet" type="text/css" href="<asset:url value='npm.font-awesome.css' />"/>
    <link rel="stylesheet" type="text/css" href="<asset:url value='common.css' />"/>
    <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />"/>
    <link rel="stylesheet" type="text/css" href="<asset:url value='login.css' />"/>
</head>

<body>

<nav class="navbar navbar-default" role="navigation">
    <div class="container">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <a class="navbar-brand" href="#">
                <spring:message code="header.title"/>
            </a>
        </div>
    </div><!-- /.container-fluid -->
</nav>

<div class="row">

    <div class="col-xs-12">

        <div class="content">

            <div class="login">

                <spring:url var="LOGIN" value="/login"/>
                <form method="post" class="login--form" action="${LOGIN}">
                    <c:if test="${param.login_error != null}">
                        <div id="login--error" class="alert alert-danger">
                            <spring:message code="login.form.error"/>
                        </div>
                    </c:if>

                    <div class="form-group">
                        <label for="username"><spring:message code="login.form.username"/></label>
                        <input class="form-control" type="text" name="username" id="username" autofocus="autofocus" value="<c:out value="${param.username}" />">
                    </div>

                    <div class="form-group">
                        <label for="password"><spring:message code="login.form.password"/></label>
                        <input class="form-control" type="password" name="password" id="password" value="<c:out value="${param.password}" />">
                    </div>

                    <div class="form-group">
                        <button class="btn btn-primary btn-block" type="submit">
                            <i class="fa fa-sign-in" aria-hidden="true"></i> <spring:message code="login.form.submit"/>
                        </button>
                    </div>

                </form>

            </div>

        </div>

    </div>
</div>

</body>

<footer>
    <div class="row">
        <div class="col-xs-12">
            <p><spring:message code="header.title"/> v${version} powered by <a href="https://synyx.de/">synyx</a></p>
        </div>
    </div>
</footer>

</html>
