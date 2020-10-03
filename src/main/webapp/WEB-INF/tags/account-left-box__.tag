<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<%@attribute name="account" type="org.synyx.urlaubsverwaltung.account.Account" required="true" %>
<%@attribute name="vacationDaysLeft" type="org.synyx.urlaubsverwaltung.account.VacationDaysLeft" required="true" %>
<%@attribute name="beforeApril" type="java.lang.Boolean" required="true" %>
<%@attribute name="className" type="java.lang.String" required="false" %>

<uv:box__ className="${className}">
    <jsp:attribute name="icon">
        <uv:box-icon className="tw-bg-green-500 tw-text-white">
            <icon:presentation-chart-bar className="tw-w-8 tw-h-8" />
        </uv:box-icon>
    </jsp:attribute>
    <jsp:body>
        <c:choose>
            <c:when test="${account != null}">
                <c:choose>
                    <c:when test="${beforeApril}">
                        <c:set var="remainingVacatioDays" value="${vacationDaysLeft.remainingVacationDays}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="remainingVacatioDays" value="${vacationDaysLeft.remainingVacationDaysNotExpiring}" />
                    </c:otherwise>
                </c:choose>
                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                    <spring:message code="person.account.vacation.left.1" />
                </span>
                <span class="tw-my-1 tw-text-lg tw-font-medium">
                    <spring:message code="person.account.vacation.left.2" arguments="${remainingVacatioDays}" />
                </span>
                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                    <spring:message
                        code="person.account.vacation.left.remaining"
                        arguments="${vacationDaysLeft.vacationDaysUsedNextYear}"
                    />
                </span>
                <c:if test="${vacationDaysLeft.vacationDaysUsedNextYear.unscaledValue() != 0}">
                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                    <spring:message
                        code="person.account.vacation.left.alreadyUsedNextYear"
                        arguments="${vacationDaysLeft.vacationDaysUsedNextYear}"
                    />
                </span>
                </c:if>
            </c:when>
            <c:otherwise>
                <span class="tw-text-sm">
                    <spring:message code='person.account.vacation.noInformation'/>
                </span>
            </c:otherwise>
        </c:choose>
    </jsp:body>
</uv:box__>

