<%-- 
    Document   : overview_app_list
    Created on : 27.02.2012, 10:25:16
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<div class="grid_12">
    <c:choose>

        <c:when test="${empty applications}">
            <spring:message code='no.apps' />
        </c:when>

        <c:otherwise>

            <%-- has css class tablesorter only because of styling, is not sortable --%>
            <table class="app-tbl centered-tbl tablesorter" cellspacing="0">
                <tr>
                    <th>
                        <spring:message code="type" />
                    </th>
                    <th>
                        <spring:message code="time" />
                    </th>
                    <!--                                    <th>
                    <spring:message code="reason" />
                </th>-->
                    <th>
                        <spring:message code="days.vac" />
                    </th>
                    <th>
                        <spring:message code="state" />
                    </th>
                    <th class="td-detail">
                        <spring:message code="table.detail" />
                    </th>
                    <th style="text-align: center">
                        <spring:message code="delete" />
                    </th>
                </tr>

                <c:forEach items="${applications}" var="app" varStatus="loopStatus">
                    <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
                        <td class="${app.vacationType}">
                            <spring:message code="${app.vacationType.vacationTypeName}"/>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${app.startDate == app.endDate}">
                                    <spring:message code="at" />&nbsp;<joda:format style="M-" value="${app.startDate}"/>, <spring:message code="${app.howLong.dayLength}" />
                                </c:when>
                                <c:otherwise>
                                    <joda:format style="M-" value="${app.startDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${app.endDate}"/>
                                </c:otherwise>    
                            </c:choose>
                        </td>
                        <!--                                        <td>
                        <c:out value="${app.reason}"/>
                    </td>-->
                        <td>
                    <fmt:formatNumber maxFractionDigits="1" value="${app.days}" />
                    </td>
                    <td>
                        <spring:message code="${app.status.state}" />
                    </td>
                    <td class="td-detail"><a href="${formUrlPrefix}/application/${app.id}"><img src="<spring:url value='/images/playlist.png' />" /></a></td>
                    <td style="text-align: center">

                        <c:if test="${app.status.number == 1}">  
                        <sec:authorize access="hasRole('role.office')">
                            <a href="${formUrlPrefix}/application/${app.id}/cancel"><img src="<spring:url value='/images/cancel.png' />" /></a>
                        </sec:authorize>
                    </c:if>

                    <c:if test="${app.status.number == 0}">
                        <sec:authorize access="hasAnyRole('role.user', 'role.boss', 'role.office')">
                            <a href="${formUrlPrefix}/application/${app.id}/cancel"><img src="<spring:url value='/images/cancel.png' />" /></a>
                        </sec:authorize>
                    </c:if>

                    </td>
                    </tr>
                </c:forEach>
            </table>

        </c:otherwise>

    </c:choose>
</div>
