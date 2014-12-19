<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<table class="list-table striped-table bordered-table">
    <tbody>

    <c:forEach items="${comments}" var="comment">
        <tr>
            <%--<td>--%>
                <%-- TODO: here should be shown a gravatar--%>
            <%--</td>--%>
            <td>
                <c:out value="${comment.nameOfCommentingPerson}"/>
            </td>
            <td>
                <spring:message code="progress.${comment.status}"/>

                <c:choose>
                    <c:when test="${comment.status == 'WAITING'}">
                        <uv:date date="${application.applicationDate}"/>
                    </c:when>
                    <c:when test="${comment.status == 'ALLOWED'}">
                        <uv:date date="${application.editedDate}"/>
                    </c:when>
                    <c:when test="${comment.status == 'REJECTED'}">
                        <uv:date date="${application.editedDate}"/>
                    </c:when>
                    <c:when test="${comment.status == 'CANCELLED'}">
                        <uv:date date="${application.cancelDate}"/>
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