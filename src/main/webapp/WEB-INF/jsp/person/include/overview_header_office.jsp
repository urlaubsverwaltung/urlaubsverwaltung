<%-- 
    Document   : overview_header_office
    Created on : 05.09.2012, 14:36:16
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="overview-header">

    <p class="heading">
        <c:out value="${person.firstName}"/>&nbsp;<c:out value="${person.lastName}"/>&nbsp;&ndash;&nbsp;<spring:message
            code="table.overview"/><c:out value="${displayYear}"/>
    </p>

    <div class="btn-group person-selector">

        <button class="btn dropdown-toggle" data-toggle="dropdown">
            <spring:message code="ov.header.person" />&nbsp;<span class="caret"></span>
        </button>

        <ul class="dropdown-menu">

            <c:forEach items="${persons}" var="person">
                <li>
                    <a href="${formUrlPrefix}/staff/<c:out value='${person.id}' />/overview">
                        <c:out value="${person.firstName}"/>&nbsp;<c:out value="${person.lastName}"/>
                    </a>
                </li>
            </c:forEach> 

        </ul>

    </div>

    <jsp:include page="../include/year_selector.jsp" />

</div>