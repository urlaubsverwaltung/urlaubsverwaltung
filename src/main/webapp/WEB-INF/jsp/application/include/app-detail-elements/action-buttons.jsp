<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<spring:url var="URL_PREFIX" value="/web"/>

<%-- SETTING VARIABLES --%>

<sec:authorize access="hasAuthority('USER')">
    <c:set var="IS_USER" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('BOSS')">
    <c:set var="IS_BOSS" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('DEPARTMENT_HEAD')">
    <c:set var="IS_DEPARTMENT_HEAD" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('SECOND_STAGE_AUTHORITY')">
    <c:set var="IS_SECOND_STAGE_AUTHORITY" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('OFFICE')">
    <c:set var="IS_OFFICE" value="${true}"/>
</sec:authorize>

<c:set var="CAN_ALLOW" value="${IS_BOSS || IS_DEPARTMENT_HEAD || IS_SECOND_STAGE_AUTHORITY}"/>
<c:set var="IS_OWN" value="${application.person.id == signedInUser.id}"/>
<c:set var="IS_SECOND_STAGE_AUTHORITY_APPLICATION"
       value="${fn:contains(application.person.permissions, 'SECOND_STAGE_AUTHORITY')}"/>


<%-- DISPLAYING DEPENDS ON VARIABLES --%>

<%-- ALLOW ACTION --%>
<c:if test="${application.status == 'WAITING' || application.status == 'TEMPORARY_ALLOWED'}">
    <c:if test="${CAN_ALLOW &&(!IS_OWN || IS_BOSS) && !(IS_SECOND_STAGE_AUTHORITY_APPLICATION && IS_DEPARTMENT_HEAD)}">
        <a href="#" class="icon-link tw-px-1 hover:tw-text-green-500" data-title="<spring:message code='action.allow'/>"
           onclick="$('#reject').hide(); $('#refer').hide(); $('#cancel').hide(); $('#allow').show();">
            <icon:check className="tw-w-5 tw-h-5" solid="true" />
        </a>
    </c:if>
</c:if>

<%-- REFER ACTION --%>
<c:if test="${application.status == 'WAITING' || application.status == 'TEMPORARY_ALLOWED'}">
    <c:if test="${CAN_ALLOW && (!IS_OWN || IS_BOSS)}">
        <a href="#" class="icon-link tw-px-1" data-title="<spring:message code='action.refer'/>"
           onclick="$('#reject').hide(); $('#allow').hide(); $('#cancel').hide(); $('#refer').show();">
            <icon:share className="tw-w-5 tw-h-5" />
        </a>
    </c:if>
</c:if>

<%-- REJECT ACTION --%>
<c:if test="${application.status == 'WAITING' || application.status == 'TEMPORARY_ALLOWED'}">
    <c:if test="${CAN_ALLOW && (!IS_OWN || IS_BOSS)}">
        <a href="#" class="icon-link tw-px-1 hover:tw-text-red-500" data-title="<spring:message code='action.reject'/>"
           onclick="$('#refer').hide(); $('#allow').hide(); $('#cancel').hide(); $('#reject').show();">
            <icon:ban className="tw-w-5 tw-h-5" solid="true" />
        </a>
    </c:if>
</c:if>

<%-- REMIND ACTION --%>
<c:if test="${application.status == 'WAITING' || application.status == 'TEMPORARY_ALLOWED'}">
    <c:if test="${IS_USER && IS_OWN && !CAN_ALLOW}">
        <a href="#" class="icon-link tw-px-1" data-title="<spring:message code='action.remind'/>" onclick="$('form#remind').submit();">
            <icon:speakerphone className="tw-w-5 tw-h-5" />
        </a>
    </c:if>
</c:if>

<%-- EDIT ACTION --%>
<c:if test="${application.status == 'WAITING'}">
    <c:if test="${IS_OWN}">
        <a href="${URL_PREFIX}/application/${application.id}/edit" class="icon-link tw-px-1" data-title="<spring:message code="action.edit"/>">
            <icon:pencil className="tw-w-5 tw-h-5" />
        </a>
    </c:if>
</c:if>

<%-- CANCEL ACTION --%>
<c:if
    test="${application.status == 'WAITING' || application.status == 'TEMPORARY_ALLOWED' || application.status == 'ALLOWED'}">
    <c:if test="${(IS_USER && IS_OWN) || IS_OFFICE}">
        <a href="#" class="icon-link tw-px-1 hover:tw-text-red-500" data-title="<spring:message code='action.delete'/>"
           onclick="$('#reject').hide(); $('#allow').hide(); $('#refer').hide(); $('#cancel').show();">
            <icon:trash className="tw-w-5 tw-h-5" />
        </a>
    </c:if>
</c:if>

<%-- PRINT ACTION --%>
<c:if test="${IS_USER}">
    <uv:print/>
</c:if>
