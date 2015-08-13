<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<c:if test="${application.status == 'WAITING' || application.status == 'ALLOWED'}">

    <c:if test="${application.status == 'WAITING'}">
        <sec:authorize access="hasRole('USER')">
            <jsp:include page="./actions/remind_form.jsp"/>
        </sec:authorize>
        <sec:authorize access="hasAnyRole('DEPARTMENT_HEAD', 'BOSS')">
            <jsp:include page="./actions/allow_form.jsp"/>
            <jsp:include page="./actions/reject_form.jsp"/>
            <jsp:include page="./actions/refer_form.jsp"/>
        </sec:authorize>
        <sec:authorize access="hasRole('USER')">
            <jsp:include page="./actions/cancel_form.jsp"/>
        </sec:authorize>
    </c:if>

    <c:if test="${application.status == 'ALLOWED'}">
        <sec:authorize access="hasRole('OFFICE')">
            <jsp:include page="./actions/cancel_form.jsp"/>
        </sec:authorize>
    </c:if>

</c:if>
