<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="account" type="org.synyx.urlaubsverwaltung.account.Account" required="true" %>
<%@attribute name="vacationDaysLeft" type="org.synyx.urlaubsverwaltung.account.VacationDaysLeft"
             required="true" %>
<%@attribute name="beforeApril" type="java.lang.Boolean" required="true" %>

<div class="box">
    <span class="box-icon bg-green">
        <i class="fa fa-bar-chart" aria-hidden="true"></i>
    </span>
    <span class="box-text">
        <c:choose>
            <c:when test="${account != null}">
                <spring:message code="person.account.vacation.left" arguments="${vacationDaysLeft.vacationDays}"/>
                <c:choose>
                    <c:when test="${beforeApril}">
                        <spring:message code="person.account.vacation.left.remaining"
                                        arguments="${vacationDaysLeft.remainingVacationDays}"/>
                    </c:when>
                    <c:otherwise>
                        <spring:message code="person.account.vacation.left.remaining"
                                        arguments="${vacationDaysLeft.remainingVacationDaysNotExpiring}"/>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${vacationDaysLeft.vacationDaysUsedNextYear.unscaledValue() != 0}">
                        <br/>
                        <spring:message code="person.account.vacation.left.alreadyUsedNextYear"
                                           arguments="${vacationDaysLeft.vacationDaysUsedNextYear}" />
                    </c:when>
                </c:choose>
            </c:when>
            <c:otherwise>
                <spring:message code='person.account.vacation.noInformation'/>
            </c:otherwise>
        </c:choose>
    </span>
</div>
