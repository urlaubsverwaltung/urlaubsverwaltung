<%-- 
    Document   : wartend
    Created on : 26.10.2011, 15:03:30
    Author     : aljona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<c:choose>

    <c:when test="${empty applications}">

        <spring:message code="no.apps" />

    </c:when>

    <c:otherwise>     

        <script type="text/javascript">
            $(document).ready(function()
                    {
                        $(".zebra-table tr").mouseover(function() {
                            $(this).addClass("over");
                        });

                        $(".zebra-table tr").mouseout(function() {
                            $(this).removeClass("over");
                        });
                    }
            );
        </script>
        
        <%-- has css class tablesorter only because of styling, no sorting here because date sorting behaves strange--%>
        <table class="app-tbl centered-tbl zebra-table tablesorter" cellspacing="0">
            <thead>
            <tr>
                <th>
                    <spring:message code="state" />
                </th>
                <th>
                    <spring:message code="${touchedDate}" />
                </th>
                <th>
                    <spring:message code="staff" />
                </th>
                <th>
                    <spring:message code="type" />
                </th>
                <th>
                    <spring:message code="time" />
                </th>
                <th>
                    <spring:message code="days.vac" />
                </th>
                <th class="td-detail">
                    <spring:message code="table.detail" />
                </th>
                <%-- the roles office and boss are able to see the application list
                but only office can cancel applications of other users --%>    
                <sec:authorize access="hasRole('role.office')">
                        <th style="text-align: center">
                            <spring:message code="delete" />
                        </th>
                        <c:if test="${showCheckboxes == true}">
                        <th>
                            <spring:message code="in.calendar" />
                        </th>
                        </c:if>
                </sec:authorize>      
            </tr>
            </thead>

            <tbody>
            <c:forEach items="${applications}" var="app" varStatus="loopStatus">
                <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
                    <td>
                        <spring:message code="${app.status.state}" />
                    </td>
                    <td>
                    
                    <%-- 0 : applications are waiting --%>
                    <c:if test="${app.status.number == 0}">
                        <c:set var="appTouched" value="${app.applicationDate}" />
                    </c:if>        

                    <%-- 1 and 2 : applications are allowed or rejected --%>
                    <c:if test="${app.status.number == 1 || app.status.number == 2}">
                        <c:set var="appTouched" value="${app.editedDate}" />
                    </c:if>

                    <%-- 3 : applications are cancelled --%>
                    <c:if test="${app.status.number == 3}">
                        <c:set var="appTouched" value="${app.cancelDate}" />
                    </c:if>
                                        
                    <joda:format style="M-" value="${appTouched}"/>
                    </td>
                    <td>
                        <c:out value="${app.person.firstName}" />&nbsp;<c:out value="${app.person.lastName}" />
                    </td>
                    <td>
                        <spring:message code="${app.vacationType.vacationTypeName}"/>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${app.startDate == app.endDate}">
                                am&nbsp;<joda:format style="M-" value="${app.startDate}"/>
                            </c:when>
                            <c:otherwise>
                                <joda:format style="M-" value="${app.startDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${app.endDate}"/>
                            </c:otherwise>    
                        </c:choose>
                    </td>
                    <td>
                        <fmt:formatNumber maxFractionDigits="1" value="${app.days}" />
                    </td>

                    <td class="td-detail"><a href="${formUrlPrefix}/application/${app.id}"><img src="<spring:url value='/images/playlist.png' />" /></a></td>     

                    <sec:authorize access="hasRole('role.office')">
                            <td style="text-align: center">
                                <c:if test="${app.status.number == 0 || app.status.number == 1}">
                                    <a href="${formUrlPrefix}/application/${app.id}/cancel"><img src="<spring:url value='/images/cancel.png' />" /></a>
                                </c:if>
                            </td>
                            <c:if test="${showCheckboxes == true}">
                            <form:form action="${formUrlPrefix}/application/allowed/${app.id}" method="PUT">
                            <td style="padding-left:2.5em;">
                                <c:choose>
                                    <c:when test="${app.isInCalendar == true}">
                                        <input type="checkbox" checked="checked" onclick="this.form.submit();" />
                                    </c:when>
                                    <c:otherwise>
                                        <input type="checkbox" onclick="this.form.submit();" />
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            </form:form>
                            </c:if>
                    </sec:authorize>     

                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise> 
</c:choose>  

