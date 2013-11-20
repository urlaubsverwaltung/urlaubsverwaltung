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

            <div class="grid_6">

                <div class="overview-header">
                    <legend>
                        <p><spring:message code="sicknote" /></p>
                        <a class="btn" style="float:right" href="${formUrlPrefix}/sicknote/${sickNote.id}/edit"><i class="icon-pencil"></i>&nbsp;<spring:message code="edit" /></a>
                    </legend>
                </div>
                
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

                <div class="overview-header">
                    <legend>
                        <p><spring:message code="progress" /></p>
                        <button class="btn" style="float:right;" onclick="$('div#comment-form').show();">
                            <i class="icon-plus"></i>&nbsp;Neuer Kommentar
                        </button>
                    </legend>
                </div>
                
                <table class="app-detail">
                    <tbody>
                        <c:forEach items="${sickNote.comments}" var="comment" varStatus="loopStatus">
                            <tr class="${loopStatus.index % 2 == 0 ? 'odd' : 'even'}">
                                <td style="width:10%"><joda:format style="M-" value="${comment.date}"/></td>
                                <td style="width:10%">
                                    <c:out value="${comment.person.firstName}" />&nbsp;<c:out value="${comment.person.lastName}" />
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${loopStatus.index == 0}">
                                            <%-- first comment is auto generated during creation of sick note --%>
                                            <spring:message code="sicknotes.created" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:out value="${comment.text}" />
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                
                <c:choose>
                    <c:when test="${error}">
                       <c:set var="STYLE" value="display: block" /> 
                    </c:when>
                    <c:otherwise>
                        <c:set var="STYLE" value="display: none" /> 
                    </c:otherwise>
                </c:choose>
                
                <div class="confirm-green" id="comment-form" style="${STYLE}">
                    <form:form method="POST" action="${formUrlPrefix}/sicknote/${sickNote.id}" modelAttribute="comment">
                        <form:errors path="text" cssClass="error" /><br />
                        <span id="text-comment"></span><spring:message code="max.chars" />
                        <form:textarea class="form-textarea" path="text" cssErrorClass="form-textarea error" onkeyup="count(this.value, 'text-comment');" onkeydown="maxChars(this,200); count(this.value, 'text-comment');" />
                        <br />
                        <button class="btn" type="submit">
                            <i class="icon-ok"></i>&nbsp;<spring:message code="save" />
                        </button>
                        <button class="btn" type="button" onclick="$('div#comment-form').hide();">
                            <i class="icon-remove"></i><spring:message code="cancel" />
                        </button>
                    </form:form> 
                </div>
                
            </div>

        </div>

    </div>

</div>

</body>
</html>