<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
    <title><spring:message code="title"/></title>
    <%@include file="../include/header.jsp" %>
</head>
<body>

<spring:url var="formUrlPrefix" value="/web"/>

<%@include file="../include/menu_header.jsp" %>

<div id="content">

    <div class="container_12">

        <div class="grid_12">

            <div class="overview-header">
                <legend>
                    <p><spring:message code="sicknote" /></p>
                </legend>
            </div>

            <div class="grid_6" style="margin-left: 0">
              <table class="app-detail">
                  <tbody>
                      <tr class="odd">
                          <td><spring:message code="name" /></td>
                          <td><c:out value="${sickNote.person.firstName}" />&nbsp;<c:out value="${sickNote.person.lastName}" /></td>
                      </tr>
                      <tr class="even">
                          <td><spring:message code="time" /></td>
                          <td><joda:format style="M-" value="${sickNote.startDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${sickNote.endDate}"/></td>
                      </tr>
                      <tr class="odd">
                          <td><spring:message code="work.days" /></td>
                          <td><fmt:formatNumber maxFractionDigits="1" value="${sickNote.workDays}" /></td>
                      </tr>
                      <tr class="even">
                          <td><spring:message code="sicknotes.aub" /></td>
                          <td>
                              <c:choose>
                                  <c:when test="${sickNote.aubPresent}">
                                    <i class="icon-ok"></i> 
                                  </c:when>
                                  <c:otherwise>
                                    <i class="icon-remove"></i>
                                  </c:otherwise>
                              </c:choose>
                          </td>
                      </tr>
                      <tr class="odd">
                          <td><spring:message code="app.date.overview" /></td>
                          <td><joda:format style="M-" value="${sickNote.lastEdited}"/></td>
                      </tr>
                  </tbody>
              </table>  
            </div>

            <div class="grid_6">
                <table class="app-detail">
                    <tbody>
                        <tr class="odd">
                            <td><b><spring:message code="progress" /></b></td>
                        </tr>
                        <tr class="even">
                            <td><c:out value="${sickNote.comment}" /></td>
                        </tr>
                    </tbody>
                </table>
            </div>

        </div>

    </div>

</div>

</body>
</html>