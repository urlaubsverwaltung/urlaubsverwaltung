<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasRole('USER')">
  <c:set var="IS_USER" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasRole('OFFICE')">
  <c:set var="IS_OFFICE" value="${true}"/>
</sec:authorize>

<c:if test="${application.status == 'WAITING' || application.status == 'ALLOWED' || application.status == 'TEMPORARY_ALLOWED' }">

    <c:if test="${application.status == 'WAITING'}">
        <sec:authorize access="hasRole('USER')">
            <jsp:include page="actions/remind_form.jsp"/>
        </sec:authorize>
        <sec:authorize access="hasAnyRole('DEPARTMENT_HEAD', 'SECOND_STAGE_AUTHORITY', 'BOSS')">
            <jsp:include page="actions/allow_form.jsp"/>
            <jsp:include page="actions/reject_form.jsp"/>
            <jsp:include page="actions/refer_form.jsp"/>
        </sec:authorize>
        <sec:authorize access="hasRole('USER')">
            <jsp:include page="actions/cancel_form.jsp"/>
        </sec:authorize>
    </c:if>

    <c:if test="${application.status == 'TEMPORARY_ALLOWED'}">
        <sec:authorize access="hasAnyRole('DEPARTMENT_HEAD', 'SECOND_STAGE_AUTHORITY', 'BOSS')">
            <jsp:include page="actions/allow_form.jsp"/>
            <jsp:include page="actions/reject_form.jsp"/>
            <jsp:include page="actions/refer_form.jsp"/>
        </sec:authorize>
    </c:if>

    <c:if test="${application.status == 'ALLOWED' || application.status == 'TEMPORARY_ALLOWED'}">
        <c:if test="${IS_OFFICE || (IS_USER && application.person.id == signedInUser.id)}">
            <jsp:include page="actions/cancel_form.jsp"/>
        </c:if>
    </c:if>
</c:if>
