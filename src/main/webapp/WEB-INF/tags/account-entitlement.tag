<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="account" type="org.synyx.urlaubsverwaltung.account.Account" required="true" %>
<%@attribute name="className" type="java.lang.String" required="false" %>

<uv:box className="${className}">
    <jsp:attribute name="icon">
        <uv:box-icon className="tw-bg-green-500 tw-text-white">
            <uv:icon-calendar className="tw-w-8 tw-h-8" />
        </uv:box-icon>
    </jsp:attribute>
    <jsp:body>
        <c:choose>
            <c:when test="${account != null}">
                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                    <spring:message code="person.account.vacation.entitlement.1" />
                </span>
                <span class="tw-my-1 tw-text-lg tw-font-medium">
                    <spring:message code="person.account.vacation.entitlement.2" arguments="${account.vacationDays}" />
                </span>
                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                    <spring:message code="person.account.vacation.entitlement.remaining" arguments="${account.remainingVacationDays}"/>
                </span>
            </c:when>
            <c:otherwise>
                <span class="tw-text-sm">
                    <spring:message code='person.account.vacation.noInformation'/>
                </span>
            </c:otherwise>
        </c:choose>
    </jsp:body>
</uv:box>
