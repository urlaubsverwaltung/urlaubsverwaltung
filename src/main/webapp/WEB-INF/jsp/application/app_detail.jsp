<%-- 
    Document   : app_detail
    Created on : 09.01.2012, 10:12:13
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>

    <head>
        <%@include file="../include/header.jsp" %>
        <title><spring:message code="title" /></title>
        <script type="text/javascript">
            function maxChars(elem, max) {
                if (elem.value.length > max) {
                    elem.value = elem.value.substring(0, max);
                }
            }
        </script>
        <!-- script to count number of chars in textarea -->
        <script type='text/javascript'>
            function count(val, id) {
                document.getElementById(id).innerHTML = val.length + "/";
            }
            
        </script>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/menu_header.jsp" %>

        <div id="content">
            <div class="container_12">

                <div class="grid_12">&nbsp;</div>


                <div class="grid_12">

                    <table class="overview-header">
                        <tr>
                            <td><spring:message code="app.title" /></td>
                        </tr>
                    </table>
                </div>

                <div class="grid_6">

                    <table class="app-detail" cellspacing="0">
                        <tr class="odd">
                            <th><c:out value="${application.person.firstName} ${application.person.lastName}" /></th>
                            <td><c:out value="${application.person.email}" /></td>
                        </tr>
                        <%@include file="./include/account_days_for_app_view.jsp" %>
                    </table>

                    <table class="app-detail tbl-margin-top" cellspacing="0">
                        <tr class="odd">
                            <th colspan="2">
                                <spring:message code="app.apply" /> <spring:message code="${application.vacationType.vacationTypeName}" />
                            </th>
                        </tr>
                        <tr class="even">
                            <td>
                                <c:choose>
                                    <c:when test="${application.startDate == application.endDate}">
                                        <spring:message code="at" /> <b><joda:format style="M-" value="${application.startDate}"/></b>
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="from" /> <b><joda:format style="M-" value="${application.startDate}"/></b> <spring:message code="to" /> <b><joda:format style="M-" value="${application.endDate}"/></b>
                                    </c:otherwise>    
                                </c:choose>
                            </td>
                            <td>
                                = <fmt:formatNumber maxFractionDigits="1" value="${application.days}"/> 
                                <c:choose>
                                    <c:when test="${application.days > 0.50 && application.days <= 1.00}">
                                        <spring:message code="day.vac" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="days.vac" />
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr class="odd">
                            <td>
                                <label for="grund"><spring:message code='reason' /></label>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${application.reason != null && !empty application.reason}">
                                        <c:out value="${application.reason}" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="not.stated" />
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr class="even">
                            <td>
                                <label for="vertreter"><spring:message code='app.rep' /></label> 
                            </td>
                            <td>
                                <c:out value="${application.rep}" />
                            </td>
                        </tr>
                        <tr class="odd">
                            <td>
                                <label for="anschrift"><spring:message code='app.address' /></label>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${application.address!= null && !empty application.address}">
                                        <c:out value="${application.address}" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="not.stated" />
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr class="even">
                            <td>
                                <label for="telefon"><spring:message code='app.phone' /></label>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${application.phone != null && !empty application.phone}">
                                        <c:out value="${application.phone}" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="not.stated" />
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </table>
                </div>

                <div class="grid_6">
                    <!-- there are four possible status, so there are max. four lines -->
                    <table class="app-detail" cellspacing="0" style="margin-bottom:2em;">
                        <tr class="odd">
                            <th colspan="2">Verlauf</th>
                        </tr>
                        <c:forEach items="${comments}" var="c" varStatus="loopStatus">
                            <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">

                                <!-- application is waiting -->
                                <c:if test="${c.status.number == 0}">
                                    <td>
                                        <spring:message code="${c.progress}" /> <joda:format style="M-" value="${application.applicationDate}"/>  
                                    </td>
                                    <td>
                                        <spring:message code="by" /> <c:out value="${c.nameOfCommentingPerson}" />
                                    </td>
                                </c:if>

                                <!-- application is allowed or rejected -->
                                <c:if test="${c.status.number == 1 || c.status.number == 2}">
                                    <td>
                                        <spring:message code="${c.progress}" /> <joda:format style="M-" value="${application.editedDate}"/>  
                                    </td>
                                    <td>
                                        <spring:message code="by" /> <c:out value="${c.nameOfCommentingPerson}" />
                                        <c:if test="${c.reason != null && not empty c.reason}">
                                            <spring:message code="app.comment" />
                                            <br />
                                            <i><c:out value="${c.reason}" /></i>
                                        </c:if>
                                    </td>
                                </c:if>

                                <!-- application is cancelled -->
                                <c:if test="${c.status.number == 3}">
                                    <td>
                                        <spring:message code="${c.progress}" /> <joda:format style="M-" value="${application.cancelDate}"/>
                                    </td>
                                    <td>
                                        <spring:message code="by" /> <c:out value="${c.nameOfCommentingPerson}" />
                                        <c:if test="${c.reason != null && not empty c.reason}">
                                            <spring:message code="app.comment" />
                                            <br />
                                            <i><c:out value="${c.reason}" /></i>
                                        </c:if>
                                    </td>
                                </c:if>

                            </tr>
                        </c:forEach>
                    </table>

                    <c:choose>
                        <c:when test="${application.person.id == loggedUser.id}">
                            <%@include file="./include/actions_user.jsp" %>
                        </c:when>
                        <c:otherwise>
                            <sec:authorize access="hasRole('role.boss')">
                                <%@include file="./include/actions_boss.jsp" %>
                            </sec:authorize>
                            <sec:authorize access="hasRole('role.office')">
                                <%@include file="./include/actions_office.jsp" %>
                            </sec:authorize>
                        </c:otherwise>
                    </c:choose>

                </div>

            </div> <!-- end of grid container -->

        </div> <!-- end of content -->

    </body>

</html>
