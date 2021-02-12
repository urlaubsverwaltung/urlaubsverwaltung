<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <jsp:attribute name="actions">
        <a href="${URL_PREFIX}/person/${person.id}${yearUrlParameterFirst}" class="icon-link tw-px-1" data-title="<spring:message code="action.details"/>">
            <icon:document-text className="tw-w-5 tw-h-5" />
        </a>
        <uv:print />
    </jsp:attribute>
    <jsp:body>
        <h1>
            <spring:message code="overview.title"/>
        </h1>
        <uv:year-selector year="${displayYear}" hrefPrefix="${URL_PREFIX}/person/${person.id}/overview?year="/>
    </jsp:body>
</uv:section-heading>
