<%-- 
    Document   : list-header
    Created on : 12.04.2012, 10:53:14
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<c:choose>
    <c:when test="${!empty param.year}">
        <c:set var="displayYear" value="${param.year}"/>
    </c:when>
    <c:otherwise>
        <c:set var="displayYear" value="${year}"/>
    </c:otherwise>
</c:choose>

<div class="overview-header">

    <legend>
        
        <p>
            <spring:message code="${titleApp}"/>&nbsp;&ndash;&nbsp;<c:out value="${displayYear}"/>
        </p>
        
        <jsp:include page="../include/year_selector.jsp" />

        <div class="btn-group person-selector">

            <button class="btn dropdown-toggle" data-toggle="dropdown">
                <i class="icon-filter"></i>
                <spring:message code="status.app"/>&nbsp;<span class="caret"></span>
            </button>

            <ul class="dropdown-menu">

                <li>
                    <a href="${linkPrefix}/all">
                        <i class="icon-th-list"></i>&nbsp;<spring:message code="all.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/waiting">
                        <i class="icon-edit"></i>&nbsp;<spring:message code="waiting.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/allowed">
                        <i class="icon-ok-circle"></i>&nbsp;<spring:message code="allow.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/rejected">
                        <i class="icon-ban-circle"></i>&nbsp;<spring:message code="reject.app" />
                    </a>
                </li>
                <li>
                    <a href="${linkPrefix}/cancelled">
                        <i class="icon-remove-circle"></i>&nbsp;<spring:message code="cancel.app" />
                    </a>
                </li>

            </ul>

        </div>
        
    </legend>
    
</div>

