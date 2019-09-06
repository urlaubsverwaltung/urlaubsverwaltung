<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1"/>
<title><spring:message code="header.title"/></title>
<link rel="shortcut icon" type="image/x-icon" href="<spring:url value='/favicon.ico?' />" />
<link rel="stylesheet" type="text/css" href="<asset:url value='npm.font-awesome.css' />" />
<link rel="stylesheet" type="text/css" href="<asset:url value='common.css' />" />
<link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" />
<script defer src="<asset:url value='runtime.js' />"></script>
<script defer src="<asset:url value='polyfill.js' />"></script>
<script defer src="<asset:url value='npm.babel.js' />"></script>
<script defer src="<asset:url value='npm.jquery.js' />"></script>
<script defer src="<asset:url value='npm.bootstrap.js' />"></script>
<script defer src="<asset:url value='npm.underscore.js' />"></script>
<script defer src="<asset:url value='npm.font-awesome.js' />"></script>
<script defer src="<asset:url value='common.js' />"></script>
