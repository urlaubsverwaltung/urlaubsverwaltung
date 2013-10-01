<%-- 
    Document   : overview_header_user
    Created on : 05.09.2012, 14:36:05
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

    <jsp:include page="../include/year_selector.jsp" />

</div>
