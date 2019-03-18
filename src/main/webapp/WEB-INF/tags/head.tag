<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1"/>
<title><spring:message code="header.title"/></title>
<link rel="shortcut icon" type="image/x-icon" href="<spring:url value='/favicon.ico?' />" />
<link rel="stylesheet" href="<spring:url value='/lib/vendor.css' />">
<link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" />
<script src="<spring:url value='/lib/underscore.min.js' />" type="text/javascript" ></script>
<script src="<spring:url value='/lib/vendor.min.js' />" type="text/javascript" ></script>
<script src="<spring:url value='/lib/date-fns.1.30.1.min.js' />" type="text/javascript" ></script>
<c:if test="${pageContext.response.locale.language.equals('de')}">
<script src="<spring:url value='/lib/date-fns.de.min.js' />" type="text/javascript" ></script>
</c:if>
<script src="<spring:url value='/js/custom.js' />" type="text/javascript" ></script>
<script src="<spring:url value='/js/actions.js' />" type="text/javascript" ></script>
<script src="<spring:url value='/js/back-button.js' />" type="text/javascript" ></script>
<script src="<spring:url value='/js/feedback.js' />" type="text/javascript" ></script>
<script src="<spring:url value='/js/sortable.js' />" type="text/javascript" ></script>
<script src="<spring:url value='/js/textarea.js' />" type="text/javascript" ></script>
<script src="<spring:url value='/js/gravatar.js' />" type="text/javascript" ></script>
<script src="<spring:url value='/js/polyfills.js' />" type="text/javascript" ></script>
