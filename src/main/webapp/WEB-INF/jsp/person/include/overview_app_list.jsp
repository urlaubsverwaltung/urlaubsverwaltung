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

                        <c:when test="${noapps == true}">
                            <spring:message code='no.apps' />
                        </c:when>

                        <c:otherwise>

                            <table class="app-tbl" cellspacing="0">
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
                                        <td>
                                            <spring:message code="${app.vacationType.vacationTypeName}"/>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${app.startDate == app.endDate}">
                                                    <spring:message code="at" />&nbsp;<joda:format style="M-" value="${app.startDate}"/>
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
                                        <sec:authorize access="hasRole('role.office')">
                                            <c:if test="${app.status.number == 0 || app.status.number == 1}">  
                                            <a href="${formUrlPrefix}/application/${app.id}/cancel"><img src="<spring:url value='/images/cancel.png' />" /></a>
                                            </c:if>
                                        </sec:authorize>
                                        <sec:authorize access="hasRole('role.user')">
                                            <c:if test="${app.status.number == 0}">  
                                            <a href="${formUrlPrefix}/application/${app.id}/cancel"><img src="<spring:url value='/images/cancel.png' />" /></a>
                                            </c:if>
                                        </sec:authorize>
                                        <sec:authorize access="hasRole('role.boss')">
                                            <c:if test="${app.status.number == 0}">  
                                            <a href="${formUrlPrefix}/application/${app.id}/cancel"><img src="<spring:url value='/images/cancel.png' />" /></a>
                                            </c:if>
                                        </sec:authorize>
                                            </td>
                                    </tr>
                                </c:forEach>
                            </table>

                        </c:otherwise>

                    </c:choose>
                </div>
