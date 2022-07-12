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
    <button class="icon-link tw-bg-transparent tw-px-1 hover:tw-text-emerald-500" data-title="<spring:message code='action.allow'/>"
       onclick="$('#reject').hide(); $('#refer').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#allow').show();">
        <icon:check className="tw-w-5 tw-h-5" solid="true" />
        <span class="tw-sr-only"><spring:message code='action.allow'/></span>
    </button>
</c:if>

<%-- ALLOW WAITING APPLICATION ACTION --%>
<c:if test="${isAllowedToAllowWaitingApplication}">
    <button class="icon-link tw-bg-transparent tw-px-1 hover:tw-text-emerald-500" data-title="<spring:message code='action.temporary_allow'/>"
       onclick="$('#reject').hide(); $('#refer').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#allow').show();">
        <icon:check className="tw-w-5 tw-h-5" solid="true" />
        <span class="tw-sr-only"><spring:message code='action.temporary_allow'/></span>
    </button>
</c:if>

<%-- REJECT ACTION --%>
<c:if test="${isAllowedToRejectApplication}">
    <button class="icon-link tw-bg-transparent tw-px-1 tw-py-0 hover:tw-text-red-500" data-title="<spring:message code='action.reject'/>"
       onclick="$('#refer').hide(); $('#allow').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#reject').show();">
        <icon:ban className="tw-w-5 tw-h-5" solid="true" />
        <span class="tw-sr-only"><spring:message code='action.reject'/></span>
    </button>
</c:if>

<%-- EDIT ACTION --%>
<c:if test="${isAllowedToEditApplication}">
    <a href="${URL_PREFIX}/application/${application.id}/edit" class="icon-link tw-px-1"
       data-title="<spring:message code="action.edit"/>" data-test-id="application-edit-button">
        <icon:pencil className="tw-w-5 tw-h-5" />
        <span class="tw-sr-only"><spring:message code='action.edit'/></span>
    </a>
</c:if>

<%-- CANCEL ACTION --%>
<c:if test="${isAllowedToRevokeApplication || isAllowedToCancelApplication}">
    <button class="icon-link tw-bg-transparent tw-px-1 hover:tw-text-red-500" data-title="<spring:message code='action.delete'/>"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#decline-cancellation-request').hide(); $('#cancel').show();">
        <icon:trash className="tw-w-5 tw-h-5" />
        <span class="tw-sr-only"><spring:message code='action.delete'/></span>
    </button>
</c:if>

<%-- CANCELLATION REQUST ACTION --%>
<c:if test="${isAllowedToStartCancellationRequest}">
    <button class="icon-link tw-bg-transparent tw-px-1 tw-py-0 hover:tw-text-red-500" data-title="<spring:message code='action.delete.request'/>"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#decline-cancellation-request').hide(); $('#cancel').show();">
        <icon:trash className="tw-w-5 tw-h-5" />
        <span class="tw-sr-only"><spring:message code='action.delete.request'/></span>
    </button>
</c:if>

<%-- DECLINE CANCELLATION REQUEST ACTION --%>
<c:if test="${isAllowedToDeclineCancellationRequest}">
    <button class="icon-link tw-bg-transparent tw-px-1 tw-py-0 hover:tw-text-red-500" data-title="<spring:message code='action.cancellationRequest'/>"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#cancel').hide(); $('#decline-cancellation-request').show();">
        <icon:ban className="tw-w-5 tw-h-5" />
        <span class="tw-sr-only"><spring:message code='action.cancellationRequest'/></span>
    </button>
</c:if>

<%-- REMIND ACTION --%>
<c:if test="${isAllowedToRemindApplication}">
    <button class="icon-link tw-bg-transparent tw-px-1 tw-py-0" data-title="<spring:message code='action.remind'/>" onclick="$('form#remind').submit();">
        <icon:speakerphone className="tw-w-5 tw-h-5" />
        <span class="tw-sr-only"><spring:message code='action.remind'/></span>
    </button>
</c:if>

<%-- REFER ACTION --%>
<c:if test="${isAllowedToReferApplication}">
    <button class="icon-link tw-bg-transparent tw-px-1 tw-py-0" data-title="<spring:message code='action.refer'/>"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#refer').show();">
        <icon:share className="tw-w-5 tw-h-5" />
        <span class="tw-sr-only"><spring:message code='action.refer'/></span>
    </button>
</c:if>

<%-- PRINT ACTION --%>
<uv:print/>
