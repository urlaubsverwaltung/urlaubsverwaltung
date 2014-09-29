<%-- 
    Document   : app_progress
    Created on : 07.09.2012, 11:20:11
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!-- there are four possible status, so there are max. four lines -->
<table class="detail-table" cellspacing="0" style="margin-top:2em; margin-bottom:2em;">
    <tr class="odd">
        <th colspan="2"><spring:message code="progress" /></th>
    </tr>
    <c:forEach items="${comments}" var="c" varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">

            <!-- application is waiting -->
            <c:if test="${c.status.number == 0}">
                <td>
                    <spring:message code="${c.progress}" /> <uv:date date="${application.applicationDate}" />  
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

            <!-- application is allowed or rejected -->
            <c:if test="${c.status.number == 1 || c.status.number == 2}">
                <td>
                    <spring:message code="${c.progress}" /> <uv:date date="${application.editedDate}" />  
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
                    <spring:message code="${c.progress}" /> <uv:date date="${application.cancelDate}" />
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

