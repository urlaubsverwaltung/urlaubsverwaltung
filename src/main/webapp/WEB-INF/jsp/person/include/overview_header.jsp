<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<legend>

    <spring:message code="overview.title"/>

    <uv:year-selector year="${displayYear}" hrefPrefix="${URL_PREFIX}/staff/${person.id}/overview?year="/>

    <span class="hidden-sm hidden-xs">
        <uv:print/>
    </span>

    <sec:authorize access="hasRole('OFFICE')">
        <span>
            <a href="${URL_PREFIX}/staff/${person.id}/edit" class="fa-action pull-right"
                data-title="<spring:message code="action.edit"/>">
                <i class="fa fa-pencil"></i>
            </a>
        </span>
    </sec:authorize>

    <span>
        <a href="${URL_PREFIX}/staff/${person.id}" class="fa-action pull-right" style="margin-top: 1px"
           data-title="<spring:message code="action.details"/>">
            <i class="fa fa-list-alt"></i>
        </a>
    </span>

    <span>
        <a href="${URL_PREFIX}/overtime" class="fa-action pull-right" data-title="<spring:message code="action.overtime.list"/>">
            <i class="fa fa-clock-o"></i>
        </a>
    </span>

</legend>
