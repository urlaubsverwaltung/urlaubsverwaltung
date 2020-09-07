<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="account" type="org.synyx.urlaubsverwaltung.account.Account" required="true" %>
<%@attribute name="className" type="java.lang.String" required="false" %>

<div class="box ${className}">
    <div class="box-icon tw-w-16 tw-h-16 tw-bg-green-500">
        <uv:icon-calendar className="tw-w-8 tw-h-8" />
    </div>
    <div class="box-text tw-text-sm">
        <c:choose>
            <c:when test="${account != null}">
                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                    <spring:message code="person.account.vacation.entitlement.1" />
                </span>
                <span class="tw-block tw-mt-2 tw-mb-1 tw-text-lg tw-font-medium">
                    <spring:message code="person.account.vacation.entitlement.2" arguments="${account.vacationDays}" />
                </span>
                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                    <spring:message code="person.account.vacation.entitlement.remaining" arguments="${account.remainingVacationDays}"/>
                </span>
            </c:when>
            <c:otherwise>
                <spring:message code='person.account.vacation.noInformation'/>
            </c:otherwise>
        </c:choose>
    </div>
</div>
