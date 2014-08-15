<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:if test="${account != null}">

    <c:choose>

    <c:when test="${empty applications}">
        <p style="margin-bottom: 4em;">
            <spring:message code='no.used.days' />
        </p>
    </c:when>
    
    <c:otherwise>

        <tr class="odd">
            <th>&nbsp;</th>
            <td class="is-centered"><b><spring:message code="state.allowed" /></b></td>
            <td class="is-centered"><b><spring:message code="state.waiting" /></b></td>
        </tr>

        <c:forEach items="${usedDaysOverview.usedDays}" var="usedDays" varStatus="loopStatus">
            <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
                <th>
                    <spring:message code="${usedDays.key.vacationTypeName}" />
                </th>
                <td class="is-centered">
                    <c:set var="days" value="${usedDays.value.map[ALLOWED] + 0}" />
                    <c:choose>
                        <c:when test="${days <= 1.00 && days > 0.50}">
                            <c:set var="numberOfDays" value="day" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="numberOfDays" value="days" />
                        </c:otherwise>
                    </c:choose>
                    <spring:message code="${numberOfDays}" arguments="${days}" />
                </td>
                <td class="is-centered">
                    <c:set var="days" value="${usedDays.value.map[WAITING] + 0}" />
                    <c:choose>
                        <c:when test="${days <= 1.00 && days > 0.50}">
                            <c:set var="numberOfDays" value="day" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="numberOfDays" value="days" />
                        </c:otherwise>
                    </c:choose>
                    <spring:message code="${numberOfDays}" arguments="${days}" />
                </td>
            </tr>
        </c:forEach>

    </c:otherwise>
    
    </c:choose>

</c:if>    

