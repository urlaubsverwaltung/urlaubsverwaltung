<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<spring:url var="URL_PREFIX" value="/web"/>

<%-- ALLOW TEMPORARY_ALLOWED APPLICATION ACTION --%>
<c:if test="${isAllowedToAllowTemporaryAllowedApplication}">
    <a href="#" class="icon-link tw-px-1 hover:tw-text-emerald-500" data-title="<spring:message code='action.allow'/>"
       onclick="$('#reject').hide(); $('#refer').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#allow').show();">
        <icon:check className="tw-w-5 tw-h-5" solid="true" />
    </a>
</c:if>

<%-- ALLOW WAITING APPLICATION ACTION --%>
<c:if test="${isAllowedToAllowWaitingApplication}">
    <a href="#" class="icon-link tw-px-1 hover:tw-text-emerald-500" data-title="<spring:message code='action.temporary_allow'/>"
       onclick="$('#reject').hide(); $('#refer').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#allow').show();">
        <icon:check className="tw-w-5 tw-h-5" solid="true" />
    </a>
</c:if>

<%-- REJECT ACTION --%>
<c:if test="${isAllowedToRejectApplication}">
    <a href="#" class="icon-link tw-px-1 hover:tw-text-red-500" data-title="<spring:message code='action.reject'/>"
       onclick="$('#refer').hide(); $('#allow').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#reject').show();">
        <icon:ban className="tw-w-5 tw-h-5" solid="true" />
    </a>
</c:if>

<%-- EDIT ACTION --%>
<c:if test="${isAllowedToEditApplication}">
    <a href="${URL_PREFIX}/application/${application.id}/edit" class="icon-link tw-px-1"
       data-title="<spring:message code="action.edit"/>" data-test-id="application-edit-button">
        <icon:pencil className="tw-w-5 tw-h-5" />
    </a>
</c:if>

<%-- CANCEL ACTION --%>
<c:if test="${isAllowedToRevokeApplication || isAllowedToCancelApplication || isAllowedToCancelDirectlyApplication}">
    <a href="#" class="icon-link tw-px-1 hover:tw-text-red-500" data-title="<spring:message code='action.delete'/>"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#decline-cancellation-request').hide(); $('#cancel').show();">
        <icon:trash className="tw-w-5 tw-h-5" />
    </a>
</c:if>

<%-- CANCELLATION REQUST ACTION --%>
<c:if test="${isAllowedToStartCancellationRequest}">
    <a href="#" class="icon-link tw-px-1 hover:tw-text-red-500" data-title="<spring:message code='action.delete.request'/>"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#decline-cancellation-request').hide(); $('#cancel').show();">
        <icon:trash className="tw-w-5 tw-h-5" />
    </a>
</c:if>

<%-- DECLINE CANCELLATION REQUEST ACTION --%>
<c:if test="${isAllowedToDeclineCancellationRequest}">
    <a href="#" class="icon-link tw-px-1 hover:tw-text-red-500" data-title="<spring:message code='action.cancellationRequest'/>"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#cancel').hide(); $('#decline-cancellation-request').show();">
        <icon:ban className="tw-w-5 tw-h-5" />
    </a>
</c:if>

<%-- REMIND ACTION --%>
<c:if test="${isAllowedToRemindApplication}">
    <a href="#" class="icon-link tw-px-1" data-title="<spring:message code='action.remind'/>" onclick="$('form#remind').submit();">
        <icon:speakerphone className="tw-w-5 tw-h-5" />
    </a>
</c:if>

<%-- REFER ACTION --%>
<c:if test="${isAllowedToReferApplication}">
    <a href="#" class="icon-link tw-px-1" data-title="<spring:message code='action.refer'/>"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#refer').show();">
        <icon:share className="tw-w-5 tw-h-5" />
    </a>
</c:if>

<%-- PRINT ACTION --%>
<uv:print/>
