
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>

    <head>
        <title><spring:message code="title" /></title>
        <%@include file="../include/header.jsp" %>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/menu_header.jsp" %>

        <div id="content">
            <div class="container_12">

                <div class="grid_12">

                    <table class="overview-header">
                        <tr>
                            <td colspan="2">
                                <spring:message code="role.management" />
                            </td>
                        </tr>
                    </table>

                    <c:choose>

                        <c:when test="${notexistent == true}">

                            <spring:message code="table.empty" />

                        </c:when>

                        <c:otherwise>
                            <table id="staff-list" cellspacing="0">
                                <tr>
                                    <th>&nbsp;</th>
                                    <th class="attributes"><spring:message code="login" /></th>
                                    <th class="attributes"><spring:message code="name" /></th>
                                    <th class="attributes"><spring:message code="email" /></th>
                                    <th class="attributes"><spring:message code="role" /></th>
                                    <th class="attributes"><spring:message code="user.state" /></th>
                                    <th><spring:message code="edit" /></th>
                                </tr>
                                <c:forEach items="${persons}" var="person" varStatus="loopStatus">
                                    <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
                                        <td><img src="<c:out value='${gravatarUrls[person]}?s=20&d=mm'/>" /></td>
                                        <td><c:out value="${person.loginName}"/></td>
                                        <td><c:out value="${person.firstName}"/>&nbsp;<c:out value="${person.lastName}"/></td>
                                        <td><a href="mailto:${person.email}"><c:out value="${person.email}"/></a></td>
                                        <td>
                                            <c:forEach items="${person.permissions}" var="perm" varStatus="status">
                                                <spring:message code="${perm.roleName}"/><c:if test="${!status.last}">, </c:if>
                                            </c:forEach>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${person.active == true}">
                                                    <spring:message code="user.activated" />
                                                </c:when>
                                                <c:otherwise>
                                                    <spring:message code="user.deactivated" />
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="td-edit"><a href="${formUrlPrefix}/management/${person.id}"><img src="<spring:url value='/images/edit.png' />" /></a></td>
                                    </tr>    
                                </c:forEach>
                            </table>
                        </c:otherwise>

                    </c:choose>

                </div>
            </div> 
        </div>        

    </body>

</html>
