<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="account" type="org.synyx.urlaubsverwaltung.account.domain.Account" required="true" %>

<div class="box">
    <span class="box-icon bg-green">
        <i class="fa fa-calendar" aria-hidden="true"></i>
    </span>
    <span class="box-text">
        <c:choose>
            <c:when test="${account != null}">
                <spring:message code="person.account.vacation.entitlement"
                                arguments="${account.vacationDays}"/>
                <spring:message code="person.account.vacation.entitlement.remaining"
                                arguments="${account.remainingVacationDays}"/>
            </c:when>
            <c:otherwise>
                <spring:message code='person.account.vacation.noInformation'/>
            </c:otherwise>
        </c:choose>
    </span>
</div>
