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
            <spring:message code="${titleApp}"/>&nbsp;&ndash;&nbsp;<c:out value="${displayYear}"/>
        </p>
        
        <uv:year-selector year="${year}" />
        
        <div class="btn-group selector">

            <button class="btn dropdown-toggle" data-toggle="dropdown">
                <i class="icon-filter"></i>
                <spring:message code="status.app"/>&nbsp;<span class="caret"></span>
            </button>

            <ul class="dropdown-menu icons-on-top">

                <li>
                    <a href="${linkPrefix}/all">
                        <i class="icon-th-list"></i>&nbsp;<spring:message code="all.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/waiting">
                        &nbsp;<b class="waiting-icon">?</b>&nbsp;<spring:message code="waiting.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/allowed">
                        <i class="icon-ok"></i>&nbsp;<spring:message code="allow.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/rejected">
                        <i class="icon-ban-circle"></i>&nbsp;<spring:message code="reject.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/cancelled">
                        <i class="icon-trash"></i>&nbsp;<spring:message code="cancel.app" />
                    </a>
                </li>

            </ul>

        </div>
        
    </legend>
    
</div>

