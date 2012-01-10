<%-- 
    Document   : staff_detail
    Created on : 31.10.2011, 11:49:53
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" /> 
        <title><spring:message code="title" /></title>
    </head>
    
    <body>
        
        <spring:url var="formUrlPrefix" value="/web" />
        
        <%@include file="../include/header.jsp" %>
        
        <div id="show-navi">
        <a href="${formUrlPrefix}/staff/list"><spring:message code="table.list" /></a>
        <a href="${formUrlPrefix}/staff/detail"><spring:message code="table.detail" /></a>
        </div>
        
        <div id="content">
        
      <c:forEach items="${persons}" var="person">
        <table id="person-tbl" cellspacing="0" border="1">
            <tr>
                <td rowspan="6"><img class="user-pic" src="<c:out value='${gravatarUrls[person]}'/>" /></td>
            </tr>
            <tr>
                <th>
                   <spring:message code="staff" />: 
                </th>
                <td>
                   <c:out value="${person.lastName}"/>&nbsp;<c:out value="${person.firstName}"/> 
                </td>
            </tr>
            <tr>
                <th>
                   <spring:message code="entitlement" />
                </th>
                <td>
                    <c:out value="${entitlements[person].vacationDays + entitlements[person].remainingVacationDays}"/>&nbsp;<spring:message code="peryear" />
                </td>
            </tr>   
            <tr>
                <th>&nbsp;</th>
                <td>
                    <a href="${formUrlPrefix}/application/${person.id}"><spring:message code="table.app" /></a>
                </td>
            </tr>   
        </table>    
        <br />        
        </c:forEach>
        
        </div>
        
    </body>
    
</html>
