<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<spring:url var="formUrlPrefix" value="/web" />

<div class="print-info--only-landscape">
    <h4><spring:message code="print.info.landscape" /></h4>
</div>

<table cellspacing="0" class="list-table striped-table selectable-table sortable tablesorter print--only-landscape">
    <thead class="hidden-xs hidden-sm">
    <tr>
        <th></th>
        <th class="sortable-field"><spring:message code="firstname" /></th>
        <th class="sortable-field"><spring:message code="lastname" /></th>
        <th class="sortable-field is-centered"><spring:message code='overview.entitlement.per.year' /></th>
        <th class="sortable-field is-centered"><spring:message code='overview.actual.entitlement' /></th>
        <th class="sortable-field is-centered"><spring:message code='overview.remaining.days.last.year' /></th>
        <th class="sortable-field is-centered"><spring:message code="left"/></th>
        <sec:authorize access="hasRole('OFFICE')">
            <th></th>
        </sec:authorize>    
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${persons}" var="person" varStatus="loopStatus">
        <tr onclick="navigate('${formUrlPrefix}/staff/${person.id}/overview');">
            <td class="is-centered">
                <img class="img-circle hidden-print" src="<c:out value='${gravatarUrls[person]}?d=mm&s=60'/>"/>
            </td>
            <td><c:out value="${person.firstName}"/></td>
            <td><c:out value="${person.lastName}"/></td>
            <td class="is-centered hidden-xs hidden-sm">
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
            <td class="is-centered hidden-xs hidden-sm">
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
            <td class="is-centered hidden-xs hidden-sm">
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
            <td class="is-centered hidden-xs hidden-sm">
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
            <td class="is-centered hidden-print"><a href="${formUrlPrefix}/staff/${person.id}/edit"><i class="fa fa-pencil fa-action" /></a></td>
            </sec:authorize>
        </tr>    
    </c:forEach>
    </tbody>
</table>

