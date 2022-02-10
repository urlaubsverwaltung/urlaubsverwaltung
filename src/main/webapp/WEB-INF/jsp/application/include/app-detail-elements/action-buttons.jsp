<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<spring:url var="URL_PREFIX" value="/web"/>

<%-- SETTING VARIABLES --%>
<c:set var="CAN_MANAGE" value="${isBoss || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson}"/>
<c:set var="IS_OWN" value="${application.person.id == signedInUser.id}"/>

<%-- ALLOW ACTION --%>
<c:set var="IS_ALLOWED_TO_ALLOW_WAITING" value="${application.status == 'WAITING' && (isBoss || ((isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson) && !IS_OWN))}"/>
<c:set var="IS_ALLOWED_TO_ALLOW_TEMPORARY_ALLOWED" value="${application.status == 'TEMPORARY_ALLOWED' && (isBoss || (isSecondStageAuthorityOfPerson && !IS_OWN))}"/>
<c:if test="${IS_ALLOWED_TO_ALLOW_WAITING || IS_ALLOWED_TO_ALLOW_TEMPORARY_ALLOWED}">
    <c:choose>
        <c:when test="${application.twoStageApproval && application.status == 'WAITING' && (isDepartmentHeadOfPerson && !IS_OWN) && !isBoss && !isSecondStageAuthorityOfPerson}">
            <c:set var="ALLOW_DATA_TITLE">
                <spring:message code='action.temporary_allow'/>
            </c:set>
        </c:when>
        <c:otherwise>
            <c:set var="ALLOW_DATA_TITLE">
                <spring:message code='action.allow'/>
            </c:set>
        </c:otherwise>
    </c:choose>

    <a href="#" class="icon-link tw-px-1 hover:tw-text-emerald-500" data-title="${ALLOW_DATA_TITLE}"
       onclick="$('#reject').hide(); $('#refer').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#allow').show();">
        <icon:check className="tw-w-5 tw-h-5" solid="true" />
    </a>
</c:if>

<%-- REJECT ACTION --%>
<c:if test="${(application.status == 'WAITING' || application.status == 'TEMPORARY_ALLOWED') && !IS_OWN && CAN_MANAGE}">
    <a href="#" class="icon-link tw-px-1 hover:tw-text-red-500" data-title="<spring:message code='action.reject'/>"
       onclick="$('#refer').hide(); $('#allow').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#reject').show();">
        <icon:ban className="tw-w-5 tw-h-5" solid="true" />
    </a>
</c:if>

<%-- EDIT ACTION --%>
<c:if test="${application.status == 'WAITING' && IS_OWN}">
    <a
        href="${URL_PREFIX}/application/${application.id}/edit"
        class="icon-link tw-px-1"
        data-title="<spring:message code="action.edit"/>"
        data-test-id="application-edit-button"
    >
        <icon:pencil className="tw-w-5 tw-h-5" />
    </a>
</c:if>

<%-- CANCEL ACTION --%>
<c:if test="${((application.status == 'WAITING' || application.status == 'TEMPORARY_ALLOWED' || application.status == 'ALLOWED') && IS_OWN) || (isOffice && (application.status == 'WAITING' || application.status == 'TEMPORARY_ALLOWED' || application.status == 'ALLOWED' || application.status == 'ALLOWED_CANCELLATION_REQUESTED'))}">

    <c:choose>
        <c:when test="${!isOffice && (application.status == 'ALLOWED' || application.status == 'TEMPORARY_ALLOWED')}">
            <c:set var="CANCEL_TITLE">
                <spring:message code='action.delete.request'/>
            </c:set>
        </c:when>
        <c:otherwise>
            <c:set var="CANCEL_TITLE">
                <spring:message code='action.delete'/>
            </c:set>
        </c:otherwise>
    </c:choose>

    <a href="#" class="icon-link tw-px-1 hover:tw-text-red-500" data-title="${CANCEL_TITLE}"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#decline-cancellation-request').hide(); $('#cancel').show();">
        <icon:trash className="tw-w-5 tw-h-5" />
    </a>
</c:if>

<%-- DECLINE CANCELLATION REQUEST ACTION --%>
<c:if test="${isOffice && application.status == 'ALLOWED_CANCELLATION_REQUESTED' }">
    <a href="#" class="icon-link tw-px-1 hover:tw-text-red-500" data-title="<spring:message code='action.cancellationRequest'/>"
       onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#cancel').hide(); $('#decline-cancellation-request').show();">
        <icon:ban className="tw-w-5 tw-h-5" />
    </a>
</c:if>

<%-- REMIND ACTION --%>
<c:if test="${application.status == 'WAITING' || application.status == 'TEMPORARY_ALLOWED'}">
    <c:if test="${IS_OWN && !CAN_MANAGE}">
        <a href="#" class="icon-link tw-px-1" data-title="<spring:message code='action.remind'/>" onclick="$('form#remind').submit();">
            <icon:speakerphone className="tw-w-5 tw-h-5" />
        </a>
    </c:if>
</c:if>

<%-- REFER ACTION --%>
<c:set var="IS_ALLOWED_TO_REFER" value="${isBoss || ((isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson) && !IS_OWN)}"/>
<c:if test="${application.status == 'WAITING' || application.status == 'TEMPORARY_ALLOWED'}">
    <c:if test="${IS_ALLOWED_TO_REFER}">
        <a href="#" class="icon-link tw-px-1" data-title="<spring:message code='action.refer'/>"
           onclick="$('#reject').hide(); $('#allow').hide(); $('#cancel').hide(); $('#decline-cancellation-request').hide(); $('#refer').show();">
            <icon:share className="tw-w-5 tw-h-5" />
        </a>
    </c:if>
</c:if>

<%-- PRINT ACTION --%>
<uv:print/>
