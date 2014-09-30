<%-- 
    Document   : app_progress
    Created on : 07.09.2012, 11:20:11
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<table class="list-table">
    <tbody>

    <c:forEach items="${comments}" var="comment">
        <tr>
            <td>
                TODO: GRAVATAR
                <img class="box-image img-circle print--invisible" src="<c:out value='${gravatar}?d=mm&s=60'/>"/>
            </td>
            <td>
                <c:out value="${comment.nameOfCommentingPerson}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${comment.status == 'WAITING'}">
                        <spring:message code="${comment.progress}"/> <uv:date date="${application.applicationDate}"/>
                    </c:when>
                    <c:when test="${comment.status == 'ALLOWED'}">
                        <spring:message code="${comment.progress}"/> <uv:date date="${application.editedDate}"/>
                    </c:when>
                    <c:when test="${comment.status == 'REJECTED'}">
                        <spring:message code="${comment.progress}"/> <uv:date date="${application.editedDate}"/>
                    </c:when>
                    <c:when test="${comment.status == 'CANCELLED'}">
                        <spring:message code="${comment.progress}"/> <uv:date date="${application.cancelDate}"/>
                    </c:when>
                </c:choose>

                <c:if test="${comment.reason != null && not empty comment.reason}">
                    <spring:message code="app.comment"/>
                    <br/>
                    <i><c:out value="${comment.reason}"/></i>
                </c:if>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>