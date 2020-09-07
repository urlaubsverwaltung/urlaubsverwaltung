<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<c:choose>
<c:when test="${!empty param.year}">
    <c:set var="displayYear" value="${param.year}"/>
</c:when>
<c:otherwise>
    <c:set var="displayYear" value="${year}"/>
</c:otherwise>
</c:choose>

<div class="tw-flex tw-items-end tw-border-b-2 tw-py-2">
    <div class="tw-flex-1 tw-text-2xl tw-font-normal tw-flex">
        <h1 class="tw-text-2xl tw-font-normal tw-m-0">
            <spring:message code="overview.title"/>
        </h1>
        <uv:year-selector year="${displayYear}" hrefPrefix="${URL_PREFIX}/person/${person.id}/overview?year="/>
    </div>
    <div class="print:tw-hidden">
        <a href="${URL_PREFIX}/person/${person.id}" class="icon-link tw-px-1" data-title="<spring:message code="action.details"/>">
            <uv:icon-document-text className="tw-w-5 tw-h-5" />
        </a>
        <uv:print />
    </div>
</div>
