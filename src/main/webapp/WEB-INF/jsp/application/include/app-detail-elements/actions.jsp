<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<c:if test="${isAllowedToRemindApplication}">
    <jsp:include page="actions/remind_form.jsp"/>
</c:if>

<c:if test="${isAllowedToAllowWaitingApplication || isAllowedToAllowTemporaryAllowedApplication}">
    <jsp:include page="actions/allow_form.jsp"/>
</c:if>

<c:if test="${isAllowedToRejectApplication}">
    <jsp:include page="actions/reject_form.jsp"/>
</c:if>

<c:if test="${isAllowedToReferApplication}">
    <jsp:include page="actions/refer_form.jsp"/>
</c:if>

<c:if test="${isAllowedToRevokeApplication || isAllowedToCancelApplication || isAllowedToStartCancellationRequest}">
    <jsp:include page="actions/cancel_form.jsp"/>
</c:if>

<c:if test="${isAllowedToDeclineCancellationRequest}">
    <jsp:include page="actions/decline_cancellation_request_form.jsp"/>
</c:if>
