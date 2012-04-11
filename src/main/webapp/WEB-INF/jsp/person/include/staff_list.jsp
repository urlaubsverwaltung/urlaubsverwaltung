<%-- 
    Document   : staff_list
    Created on : 31.10.2011, 11:49:42
    Author     : Aljona Murygina
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<table id="staff-list" cellspacing="0">
    <tr>
        <th>&nbsp;</th>
        <th class="attributes"><spring:message code="login" /></th>
        <th class="attributes"><spring:message code="name" /></th>
        <th class="attributes"><spring:message code="email" /></th>
        <th class="vac"><spring:message code="entitlement" />&nbsp;<spring:message code="whole" /></th>
        <th class="vac"><spring:message code="overview.left" /></th>
        <th><spring:message code="table.detail" /></th>
        <th><spring:message code="table.apply" /></th>
        <th><spring:message code="edit" /></th>
    </tr>
    <c:forEach items="${persons}" var="person" varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
            <td><img src="<c:out value='${gravatarUrls[person]}?s=20&d=mm'/>" /></td>
            <td><c:out value="${person.loginName}"/></td>
            <td><c:out value="${person.firstName}"/>&nbsp;<c:out value="${person.lastName}"/></td>
            <td><a href="mailto:${person.email}"><c:out value="${person.email}"/></a></td>
            <td class="vac">
                <c:choose>
                    <c:when test="${entitlements[person] != null}">
                        <fmt:formatNumber maxFractionDigits="1" value="${entitlements[person].vacationDays}"/> +
                        <fmt:formatNumber maxFractionDigits="1" value="${entitlements[person].remainingVacationDays}"/>
                    </c:when>
                    <c:otherwise>
                        <spring:message code='not.specified' />
                    </c:otherwise>    
                </c:choose>
            </td>
            <td class="vac">
                <c:choose>
                    <c:when test="${accounts[person] != null}">
                                <fmt:formatNumber maxFractionDigits="1" value="${accounts[person].vacationDays}"/> +
                                <fmt:formatNumber maxFractionDigits="1" value="${accounts[person].remainingVacationDays}"/>
                    </c:when>
                    <c:otherwise>
                        <spring:message code='not.specified' />
                    </c:otherwise>    
                </c:choose>
            </td>
            <td class="td-detail"><a href="${formUrlPrefix}/staff/${person.id}/overview"><img src="<spring:url value='/images/playlist.png' />" /></a></td>
            <td class="td-detail"><a href="${formUrlPrefix}/${person.id}/application/new"><img src="<spring:url value='/images/new window.png' />" /></a></td>
            <td class="td-edit"><a href="${formUrlPrefix}/staff/${person.id}/edit"><img src="<spring:url value='/images/edit.png' />" /></a></td>
        </tr>    
    </c:forEach>
<!--        <tr>
            <td colspan="8" style="border-top: 1px solid #EAF2D3">&nbsp;</td>
        </tr>-->
</table>

