<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="fromDate">
    <uv:date date="${from}" />
</c:set>
<c:set var="toDate">
    <uv:date date="${to}" />
</c:set>

<a href="/web/application/statistics/download?from=${fromDate}&to=${toDate}" class="icon-link tw-px-1" data-title="<spring:message code='action.download' />">
    <uv:icon-download className="tw-w-5 tw-h-5" />
</a>
