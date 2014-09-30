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
        
        <p>
            <spring:message code="${titleApp}"/> <spring:message code="in.year" /> <c:out value="${displayYear}"/>
        </p>

        <uv:year-selector year="${year}" />

        <div class="btn-group pull-right">

            <button class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                <i class="fa fa-filter"></i>
                <spring:message code="status.app"/>&nbsp;<span class="caret"></span>
            </button>

            <ul class="dropdown-menu icons-on-top">

                <li>
                    <a href="${linkPrefix}/all">
                        <i class="fa fa-calendar"></i>&nbsp;<spring:message code="all.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/waiting">
                        <i class="fa fa-question"></i>&nbsp;<spring:message code="waiting.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/allowed">
                        <i class="fa fa-check"></i>&nbsp;<spring:message code="allow.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/rejected">
                        <i class="fa fa-ban"></i>&nbsp;<spring:message code="reject.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/cancelled">
                        <i class="fa fa-trash"></i>&nbsp;<spring:message code="cancel.app" />
                    </a>
                </li>

            </ul>

        </div>
        
    </legend>
    
</div>

