<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<div class="header">

    <legend>
        
        <p>
            <spring:message code="table.overview"/> <c:out value="${displayYear}"/>

            <span class="hidden-xs">
                <uv:year-selector year="${year}" />
            </span>

            <div class="btn-group pull-right">

                <button class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                    <i class="fa fa-user"></i>
                    <spring:message code="ov.header.person" />&nbsp;<span class="caret"></span>
                </button>

                <ul class="dropdown-menu">

                    <c:forEach items="${persons}" var="person">
                        <li>
                            <a href="${formUrlPrefix}/staff/<c:out value='${person.id}' />/overview">
                                <c:out value="${person.niceName}"/>
                            </a>
                        </li>
                    </c:forEach>

                </ul>

            </div>

            <sec:authorize access="hasRole('OFFICE')">
                <a href="${formUrlPrefix}/staff/${person.id}/edit" class="btn btn-default pull-right"><i class="fa fa-pencil"></i> Edit</a>
            </sec:authorize>

            <span class="hidden-sm hidden-xs">
                <uv:print />
            </span>

        </p>

    </legend>
    
</div>