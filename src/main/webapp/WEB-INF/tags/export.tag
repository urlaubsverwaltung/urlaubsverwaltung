<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="fromDate">
    <uv:date date="${from}" pattern="yyyy-MM-dd" />
</c:set>
<c:set var="toDate">
    <uv:date date="${to}" pattern="yyyy-MM-dd" />
</c:set>

<a href="/web/application/statistics/download?from=${fromDate}&to=${toDate}" class="icon-link tw-px-1" data-title="<spring:message code='action.download' />">
    <icon:download className="tw-w-5 tw-h-5" />
</a>
