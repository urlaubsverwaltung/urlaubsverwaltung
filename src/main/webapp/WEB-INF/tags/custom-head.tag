<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1"/>
<title><spring:message code="header.title"/></title>

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
<c:choose>
    <c:when test="${theme == 'dark'}">
        <meta name="theme-color" content="#18181b">
    </c:when>
    <c:otherwise>
        <meta name="theme-color" content="#fafafa">
    </c:otherwise>
</c:choose>

<link rel="preload" as="script" href="<asset:url value='npm.jquery.js' />" crossorigin />
<link rel="preload" as="script" href="<asset:url value='npm.bootstrap.js' />" crossorigin />
<link rel="preload" as="script" href="<asset:url value='npm.underscore.js' />" crossorigin />
<link rel="preload" as="script" href="<asset:url value='npm.datalist_polyfill.js' />" crossorigin />
<link rel="preload" as="script" href="<asset:url value='npm.date-fns.js' />" crossorigin />
<c:if test="${language == 'de'}"><link rel="preload" as="script" href="<asset:url value='npm.date-fns.de.js'/>" crossorigin /></c:if>
<c:if test="${language == 'en'}"><link rel="preload" as="script" href="<asset:url value='npm.date-fns.en.js'/>" crossorigin /></c:if>
<c:if test="${language == 'el'}"><link rel="preload" as="script" href="<asset:url value='npm.date-fns.el.js'/>" crossorigin /></c:if>

<link rel="stylesheet" type="text/css" href="<asset:url value='common.css' />"/>
<link rel="stylesheet" type="text/css" href="<spring:url value='/css/style.css' />"/>
<link rel="stylesheet" type="text/css" href="<asset:url value='datepicker.css' />" />
<uv:script-theme-toggler />

<script type="module" src="<asset:url value='common.js' />"></script>
<script type="module" src="<asset:url value="date_fns_localized.js" />"></script>
<script type="modele" src="<asset:url value='custom_elements_polyfill.js' />"></script>
