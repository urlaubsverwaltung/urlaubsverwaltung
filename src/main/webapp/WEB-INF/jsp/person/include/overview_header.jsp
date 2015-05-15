<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<div class="header">

    <legend>

        <p><spring:message code="table.overview"/></p>

        <uv:year-selector year="${displayYear}"/>

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

    </legend>

</div>
