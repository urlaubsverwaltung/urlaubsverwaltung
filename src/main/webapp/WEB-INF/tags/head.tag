<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="assets" uri = "/WEB-INF/assets.tld"%>

<assets:hash var="npm_font_awesome_css" asset="npm.font-awesome.css" />
<assets:hash var="common_css" asset="common.css" />
<assets:hash var="runtime_js" asset="runtime.js" />
<assets:hash var="polyfill_js" asset="polyfill.js" />
<assets:hash var="npm_babel_js" asset="npm.babel.js" />
<assets:hash var="npm_core_js" asset="npm.core-js.js" />
<assets:hash var="npm_jquery_js" asset="npm.jquery.js" />
<assets:hash var="npm_bootstrap_js" asset="npm.bootstrap.js" />
<assets:hash var="npm_underscore_js" asset="npm.underscore.js" />
<assets:hash var="npm_font_awesome_js" asset="npm.font-awesome.js" />
<assets:hash var="common_js" asset="common.js" />

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1"/>
<title><spring:message code="header.title"/></title>
<link rel="shortcut icon" type="image/x-icon" href="<spring:url value='/favicon.ico?' />" />
<link rel="stylesheet" type="text/css" href="<spring:url value='${npm_font_awesome_css}' />" />
<link rel="stylesheet" type="text/css" href="<spring:url value='${common_css}' />" />
<link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" />
<script defer src="<spring:url value='${runtime_js}' />"></script>
<script defer src="<spring:url value='${polyfill_js}' />"></script>
<script defer src="<spring:url value='${npm_babel_js}' />"></script>
<script defer src="<spring:url value='${npm_core_js}' />"></script>
<script defer src="<spring:url value='${npm_jquery_js}' />"></script>
<script defer src="<spring:url value='${npm_bootstrap_js}' />"></script>
<script defer src="<spring:url value='${npm_underscore_js}' />"></script>
<script defer src="<spring:url value='${npm_font_awesome_js}' />"></script>
<script defer src="<spring:url value='${common_js}' />"></script>
