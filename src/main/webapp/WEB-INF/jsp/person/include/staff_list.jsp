<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url var="formUrlPrefix" value="/web" />

<sec:authorize access="hasRole('OFFICE')">
    <a class="btn btn-right" href="${formUrlPrefix}/staff/new"><i class="icon-plus"></i><i class="icon-user"></i>&nbsp;<spring:message code="table.new.person" /></a>
</sec:authorize>

<table cellspacing="0" class="data-table sortable tablesorter zebra-table">
    <thead>
    <tr>
        <th colspan="2"><spring:message code="firstname" /></th>
        <th><spring:message code="lastname" /></th>
        <th class="is-centered"><spring:message code='overview.entitlement.per.year' /></th>
        <th class="is-centered"><spring:message code='overview.actual.entitlement' /></th>
        <th class="is-centered"><spring:message code='overview.remaining.days.last.year' /></th>
        <th class="is-centered"><spring:message code="left"/></th>
        <sec:authorize access="hasRole('OFFICE')">
            <th class="print--invisible"><spring:message code="table.apply" /></th>
            <th class="print--invisible"><spring:message code="edit" /></th>
        </sec:authorize>    
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${persons}" var="person" varStatus="loopStatus">
        <tr onclick="navigate('${formUrlPrefix}/staff/${person.id}/overview');">
            <td class="is-centered"><img class="print--invisible" src="<c:out value='${gravatarUrls[person]}?s=20&d=mm'/>" /></td>
            <td><c:out value="${person.firstName}"/></td>
            <td><c:out value="${person.lastName}"/></td>
            <td class="is-centered">
                <c:choose>
                    <c:when test="${accounts[person] != null}">
                        <fmt:formatNumber maxFractionDigits="1"
                                          value="${accounts[person].annualVacationDays}"/>
                    </c:when>
                    <c:otherwise>
                        <spring:message code='not.specified'/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td class="is-centered">
                <c:choose>
                    <c:when test="${accounts[person] != null}">
                        <fmt:formatNumber maxFractionDigits="1"
                                          value="${accounts[person].vacationDays}"/>
                    </c:when>
                    <c:otherwise>
                        <spring:message code='not.specified'/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td class="is-centered">
                <c:choose>
                    <c:when test="${accounts[person] != null}">
                        <fmt:formatNumber maxFractionDigits="1"
                                          value="${accounts[person].remainingVacationDays}"/>
                    </c:when>
                    <c:otherwise>
                        <spring:message code='not.specified'/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td class="is-centered">
                <c:choose>
                    <c:when test="${leftDays[person] != null && remLeftDays[person] != null}">
                        <fmt:formatNumber maxFractionDigits="1" value="${leftDays[person]}"/>
                        <c:if test="${beforeApril || !accounts[person].remainingVacationDaysExpire}">
                            +
                            <fmt:formatNumber maxFractionDigits="1" value="${remLeftDays[person]}"/>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <spring:message code='not.specified'/>
                    </c:otherwise>
                </c:choose>
            </td>
            <sec:authorize access="hasRole('OFFICE')">
            <td class="is-centered print--invisible"><a href="${formUrlPrefix}/${person.id}/application/new"><img src="<spring:url value='/images/new window.png' />" /></a></td>
            <td class="is-centered print--invisible"><a href="${formUrlPrefix}/staff/${person.id}/edit"><img src="<spring:url value='/images/edit.png' />" /></a></td>
            </sec:authorize>
        </tr>    
    </c:forEach>
    </tbody>
</table>

