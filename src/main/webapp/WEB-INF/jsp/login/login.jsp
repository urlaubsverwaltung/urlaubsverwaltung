<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title><spring:message code="login.title"/></title>

    <link rel="manifest" href="<spring:url value='/manifest.json' />"/>
    <link rel="icon" type="image/png" href="<spring:url value='/favicons/favicon.png' />"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon.png' />"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-57x57.png' />" sizes="57x57"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-60x60.png' />" sizes="60x60"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-72x72.png' />" sizes="72x72"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-76x76.png' />" sizes="76x76"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-114x114.png' />" sizes="114x114"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-120x120.png' />" sizes="120x120"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-128x128.png' />" sizes="128x128"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-144x144.png' />" sizes="144x144"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-152x152.png' />" sizes="152x152"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-180x180.png' />" sizes="180x180"/>
    <link rel="apple-touch-icon" href="<spring:url value='/favicons/apple-icon-precomposed.png' />"/>
    <link rel="icon" type="image/png" sizes="16x16" href="<spring:url value='/favicons/favicon-16x16.png' />"/>
    <link rel="icon" type="image/png" sizes="32x32" href="<spring:url value='/favicons/favicon-32x32.png' />"/>
    <link rel="icon" type="image/png" sizes="96x96" href="<spring:url value='/favicons/favicon-96x96.png' />"/>
    <link rel="icon" type="image/png" sizes="160x160" href="<spring:url value='/favicons/favicon-160x160.png' />"/>
    <link rel="icon" type="image/png" sizes="192x192" href="<spring:url value='/favicons/favicon-192x192.png' />"/>
    <link rel="icon" type="image/png" sizes="196x196" href="<spring:url value='/favicons/favicon-196x196.png' />"/>
    <meta name="msapplication-TileColor" content="#ffffff">
    <meta name="msapplication-TileImage" content="<spring:url value='/favicons/ms-icon-144x144.png' />"/>
    <meta name="theme-color" content="#ffffff">

    <link rel="stylesheet" type="text/css" href="<asset:url value='common.css' />"/>
    <link rel="stylesheet" type="text/css" href="<spring:url value='/css/style.css' />"/>
    <link rel="stylesheet" type="text/css" href="<spring:url value='/css/login.css' />"/>
</head>

<body>

<nav role="navigation">
    <h1 class="tw-text-3xl tw-my-8 tw-text-center">
        <spring:message code="header.title"/>
    </h1>
</nav>

<div class="col-xs-12">
    <div class="row">
        <div class="login">

            <spring:url var="LOGIN" value="/login"/>
            <form:form method="post" class="login--form" action="${LOGIN}">
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
                    <button class="button-main tw-w-full" type="submit">
                        <span class="tw-flex tw-items-center tw-justify-center">
                            <icon:login className="tw-w-5 tw-h-5" />
                            &nbsp;<spring:message code="login.form.submit"/>
                        </span>
                    </button>
                </div>
            </form:form>

        </div>
    </div>
</div>

<footer>
    <div class="col-xs-12">
        <div class="row">
            <span class="tw-text-sm">
                <a href="https://urlaubsverwaltung.cloud/">urlaubsverwaltung.cloud</a> |
            </span>
            <span class="tw-text-xs">
                v${version}
            </span>
        </div>
    </div>
</footer>

</body>
</html>
