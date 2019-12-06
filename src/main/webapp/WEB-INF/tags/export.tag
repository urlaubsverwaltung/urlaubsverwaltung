<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="fromDate">
	<uv:date date="${from}" />
</c:set>
<c:set var="toDate">
	<uv:date date="${to}" />
</c:set>
<a
	href="/web/application/statistics/download?from=${fromDate}&to=${toDate}"
	class="fa-action pull-right hidden-xs hidden-sm"
	data-title="<spring:message code='action.download' />"> <i
	class="fa fa-download"></i>
</a>
