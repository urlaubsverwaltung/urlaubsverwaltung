<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
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

<div class="header">

    <legend>

        <div class="legend-dropdown dropdown">
            <a id="dropdownLabel" data-target="#" href="#" data-toggle="dropdown"
               aria-haspopup="true" role="button" aria-expanded="false">
                <spring:message code="${titleApp}"/><span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownLabel">
                <li>
                    <a href="${linkPrefix}/all?year=${displayYear}">
                        <i class="fa fa-calendar"></i>&nbsp;<spring:message code="applications.all" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/waiting?year=${displayYear}">
                        <i class="fa fa-question"></i>&nbsp;<spring:message code="applications.waiting" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/allowed?year=${displayYear}">
                        <i class="fa fa-check"></i>&nbsp;<spring:message code="applications.allowed" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/rejected?year=${displayYear}">
                        <i class="fa fa-ban"></i>&nbsp;<spring:message code="applications.rejected" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/cancelled?year=${displayYear}">
                        <i class="fa fa-trash"></i>&nbsp;<spring:message code="applications.cancelled" />
                    </a>
                </li>
            </ul>
        </div>

        <p>
            <spring:message code="for" />
        </p>

        <uv:year-selector year="${displayYear}" />

    </legend>
    
</div>

