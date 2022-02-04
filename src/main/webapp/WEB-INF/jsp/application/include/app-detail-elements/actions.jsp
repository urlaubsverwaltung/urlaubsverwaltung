<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<c:set var="CAN_MANAGE" value="${isBoss || isDepartmentHead || isSecondStageAuthority}"/>
<c:set var="IS_OWN" value="${application.person.id == signedInUser.id}"/>
<c:set var="IS_ALLOWED_TO_ALLOW" value="${isBoss || ((isDepartmentHead || isSecondStageAuthority) && !IS_OWN)}"/>

<c:if test="${application.status == 'ALLOWED'}">
    <c:if test="${isOffice || IS_OWN}">
        <jsp:include page="actions/cancel_form.jsp"/>
    </c:if>
</c:if>

<c:if test="${application.status == 'WAITING'}">
    <c:if test="${IS_OWN && !CAN_MANAGE}">
        <jsp:include page="actions/remind_form.jsp"/>
    </c:if>
    <c:if test="${IS_ALLOWED_TO_ALLOW}">
        <jsp:include page="actions/allow_form.jsp"/>
        <jsp:include page="actions/reject_form.jsp"/>
        <jsp:include page="actions/refer_form.jsp"/>
    </c:if>
    <c:if test="${isOffice || IS_OWN}">
        <jsp:include page="actions/cancel_form.jsp"/>
    </c:if>
</c:if>

<c:if test="${application.status == 'TEMPORARY_ALLOWED'}">
    <c:if test="${IS_OWN && !CAN_MANAGE}">
        <jsp:include page="actions/remind_form.jsp"/>
    </c:if>
    <c:if test="${IS_ALLOWED_TO_ALLOW}">
        <jsp:include page="actions/allow_form.jsp"/>
        <jsp:include page="actions/reject_form.jsp"/>
        <jsp:include page="actions/refer_form.jsp"/>
    </c:if>
    <c:if test="${isOffice || IS_OWN}">
        <jsp:include page="actions/cancel_form.jsp"/>
    </c:if>
</c:if>

<c:if test="${application.status == 'ALLOWED_CANCELLATION_REQUESTED'}">
    <c:if test="${isOffice}">
        <jsp:include page="actions/cancel_form.jsp"/>
    </c:if>
    <c:if test="${isOffice}">
        <jsp:include page="actions/decline_cancellation_request_form.jsp"/>
    </c:if>
</c:if>
