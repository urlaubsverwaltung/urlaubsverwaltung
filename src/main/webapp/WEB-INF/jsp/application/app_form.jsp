<%-- 
    Document   : app_form
    Created on : 26.10.2011, 15:05:51
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<!DOCTYPE html>
<html>

    <head>
        <title><spring:message code="title" /></title>
        <%@include file="../include/header.jsp" %>

        <%@include file="./include/app-form-elements/datepicker.jsp" %>
        <%@include file="./include/app-form-elements/day-length-selector.jsp" %>

        <script type="text/javascript">
            $(document).ready(function() {
                $('#error-div').show();
            });
        </script>
        <style type="text/css">
            .app-detail th {
                width: 36%;
            }
        </style>

    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/menu_header.jsp" %>

        <div id="content">

            <div class="container_12">

                <c:choose>

                    <c:when test="${notpossible == true}">

                        <spring:message code="app.not.possible" />

                    </c:when>

                    <c:otherwise>

                        <div class="grid_12">

                            <div class="overview-header">

                                <legend>
                                    <p>
                                        <spring:message code="app.title" />  
                                    </p>
                                </legend>

                            </div>
                            
                        </div>

                        <c:choose>
                            <c:when test="${setForce != null}">
                                <c:set var="forcy" value="${setForce}" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="forcy" value="0" />
                            </c:otherwise>
                        </c:choose>

                        <c:choose>
                            <c:when test="${person.id == loggedUser.id}">
                                <c:set var="appliesAsRep" value="false" />
                                <c:set var="actionUrl" value="${formUrlPrefix}/application/new?force=${forcy}" />
                            </c:when>
                            <c:otherwise>
                                <sec:authorize access="hasRole('role.office')">
                                    <c:set var="appliesAsRep" value="true" />
                                    <c:set var="actionUrl" value="${formUrlPrefix}/${person.id}/application/new?force=${forcy}" />
                                </sec:authorize>
                            </c:otherwise>
                        </c:choose>


                        <form:form method="post" action="${actionUrl}" modelAttribute="appForm"> 

                            <div class="grid_6" style="margin-bottom: 4em;">

                                <c:if test="${not empty errors || timeError != null}">

                                    <div id="error-div">
                                        <c:if test="${empty errors}">
                                            <spring:message code="${timeError}" />
                                        </c:if>
                                        <form:errors cssClass="error" />
                                        <c:if test="${daysApp != null}">
                                            <span class="error">
                                                <c:choose>
                                                    <c:when test="${daysApp <= 1.00 && daysApp > 0.50}">
                                                        <c:set var="msg1" value="error.days.start.sing" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:set var="msg1" value="error.days.start.plural" />
                                                    </c:otherwise>
                                                </c:choose>
                                                <c:set var="numberOfDays" value="${leftDays}" />
                                                <c:choose>
                                                    <c:when test="${numberOfDays <= 1.00 && numberOfDays > 0.50}">
                                                        <c:set var="msg2" value="error.days.end.sing" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:set var="msg2" value="error.days.end.plural" />
                                                    </c:otherwise>
                                                </c:choose>
                                                <spring:message code="${msg2}" arguments="${numberOfDays}" />
                                                <span/>
                                            </c:if>
                                    </div>
                                </c:if>

                                <table class="app-detail" cellspacing="0">
                                    <tr class="odd">
                                        <th>
                                            <spring:message code="app.apply" />
                                        </th>
                                        <td>
                                            <form:select path="vacationType" size="1" class="form-select" onchange="checkSonderurlaub(value);">
                                                <c:choose>
                                                    <c:when test="${appForm.vacationType == null}">
                                                        <c:forEach items="${vacTypes}" var="vacType">
                                                    <option value="${vacType}">
                                                        <spring:message code='${vacType.vacationTypeName}' />
                                                    </option>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <option value="${appForm.vacationType}" selected="selected">
                                                    <spring:message code='${appForm.vacationType.vacationTypeName}' />
                                                </option>
                                                <c:forEach items="${vacTypes}" var="vacType">
                                                    <c:if test="${vacType != appForm.vacationType}">
                                                        <option value="${vacType}">
                                                            <spring:message code='${vacType.vacationTypeName}' />
                                                        </option>
                                                    </c:if>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </form:select>

                                            <script type="text/javascript">

                                                $(document).ready(function() {
                                                    $('#special-leave-info').popover(); 
                                                });
                                                
                                            </script>

                                            <img id="special-leave-info" src="<spring:url value='/images/info.png' />" style="vertical-align: middle" data-placement="bottom" data-toggle="popover" data-original-title="<spring:message code='special.leave.title.short' />" data-content="<spring:message code='special.leave.explanation' />" data-trigger="hover" />
                                    </td>

                                        <!-- Modal -->
                                        <div id="special-leave-modal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                                            <div class="modal-header">
                                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                                                <h3 id="myModalLabel"><spring:message code='special.leave.title.long' /></h3>
                                            </div>
                                            <div class="modal-body">
                                                <p><spring:message code='special.leave.explanation' /></p>
                                            </div>
                                            <div class="modal-footer">
                                                <button class="btn" data-dismiss="modal" aria-hidden="true">Schließen</button>
                                            </div>
                                        </div>
                                        
                                        
                                        
                                    </tr>
                                    <tr class="even">
                                        <td>
                                            <form:radiobutton path="howLong" checked="checked" value="${full}" onclick="$('#full-day').show(); $('#half-day').hide();" /><spring:message code='${full.dayLength}' /> 
                                            &nbsp;<form:radiobutton path="howLong" value="${morning}" onclick="$('#full-day').hide(); $('#half-day').show();" /><spring:message code='${morning.dayLength}' />
                                            &nbsp;<form:radiobutton path="howLong" value="${noon}" onclick="$('#full-day').hide(); $('#half-day').show();" /><spring:message code='${noon.dayLength}' />
                                        </td>
                                        <td>
                                            <span id="full-day">
                                                Von: <form:input id="from" path="startDate" cssErrorClass="error" style="width: 28%" />
                                                &nbsp;
                                                Bis: <form:input id="to" path="endDate" cssErrorClass="error" style="width: 28%" />
                                            </span>

                                            <span id="half-day" style="display: none">
                                                Am: <form:input id="at" path="startDateHalf" cssErrorClass="error" style="width: 27%" />
                                            </span>
                                            <br />
                                            <%-- this span with class days is filled by datepicker --%>
                                            <span class="days"></span>
                                        </td>
                                    </tr>
                                    <tr class="odd">
                                        <td>
                                            <label for="reason">
                                                <spring:message code='reason' />:
                                                <br />
                                                <spring:message code='app.reason.describe' />
                                            </label>
                                            <form:errors path="reason" cssClass="error" />
                                        </td>
                                        <td>
                                            <span id="text-reason"></span>200 Zeichen
                                            <form:textarea id="reason" path="reason" cssErrorClass="error form-textarea" class="form-textarea"
                                                           onkeyup="count(this.value, 'text-reason');" onkeydown="maxChars(this,200); count(this.value, 'text-reason');" />
                                        </td>
                                    </tr>
                                    <tr class="even">
                                        <td>
                                            <label for="vertreter"><spring:message code='app.rep' />:</label> 
                                        </td>
                                        <td>
                                            <form:select path="rep" id="vertreter" size="1" class="form-select">
                                                <c:choose>
                                                    <c:when test="${appForm.rep == null}">
                                                <option value="<spring:message code='app.no.rep' />"><spring:message code='app.no.rep' /></option>
                                                <c:forEach items="${persons}" var="einmitarbeiter">
                                                    <option value="${einmitarbeiter.firstName} ${einmitarbeiter.lastName}">
                                                        <c:out value="${einmitarbeiter.firstName}" />&nbsp;<c:out value='${einmitarbeiter.lastName}' />
                                                    </option>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <option value="${appForm.rep}" selected="selected">
                                                    <c:out value="${appForm.rep}" />
                                                </option>
                                                <option value="<spring:message code='app.no.rep' />"><spring:message code='app.no.rep' /></option>
                                                <c:forEach items="${persons}" var="einmitarbeiter">
                                                    <c:if test="${!(fn:contains(appForm.rep, einmitarbeiter.lastName) && fn:contains(appForm.rep, einmitarbeiter.firstName))}">
                                                        <option value="${einmitarbeiter.firstName} ${einmitarbeiter.lastName}">
                                                            <c:out value="${einmitarbeiter.firstName}" />&nbsp;<c:out value='${einmitarbeiter.lastName}' />
                                                        </option>
                                                    </c:if>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>

                                    </form:select>  
                                    </td>
                                    </tr>
                                    <tr class="odd">
                                        <td>
                                            <label for="anschrift">
                                                <spring:message code='app.address' />:
                                                <br />
                                                <spring:message code='app.optional' />
                                            </label>
                                            <form:errors path="address" cssClass="error" />
                                        </td>
                                        <td>
                                            <span id="text-address"></span><spring:message code="max.chars" />
                                            <form:textarea id="anschrift" path="address" class="form-textarea" cssErrorClass="error form-textarea" onkeyup="count(this.value, 'text-address');" onkeydown="maxChars(this,200); count(this.value, 'text-address');" />
                                        </td>
                                    </tr>
                                    <tr class="even">
                                        <td>
                                            <label><spring:message code='app.team' /></label>
                                        </td>
                                        <td>
                                            <form:radiobutton path="teamInformed" value="true" />&nbsp;<spring:message code='yes' />&nbsp;&nbsp;
                                            <form:radiobutton path="teamInformed" value="false" />&nbsp;<spring:message code='no' /> 
                                        </td>
                                    </tr>
                                    <tr class="odd">
                                        <td>
                                            <label for="kommentar">
                                                <spring:message code='app.form.comment' />:
                                                <br />
                                                <spring:message code='app.optional' />
                                            </label>
                                            <form:errors path="comment" cssClass="error" />
                                        </td>
                                        <td>
                                            <span id="text-comment"></span><spring:message code="max.chars" />
                                            <form:textarea id="kommentar" path="comment" class="form-textarea" cssErrorClass="error form-textarea" onkeyup="count(this.value, 'text-comment');" onkeydown="maxChars(this,200); count(this.value, 'text-comment');" />
                                        </td>
                                    </tr>
                                </table>

                                <table class="app-detail tbl-margin-top" cellspacing="0">
                                    <tr class="odd">
                                        <td>
                                            <spring:message code='app.footer' />&nbsp;<joda:format style="M-" value="${date}"/>
                                        </td>
                                        <td>
                                            <button type="submit" class="btn btn-primary" style="float: right;">
                                                <i class='icon-ok icon-white'></i>&nbsp;<spring:message code='apply' />
                                            </button>
                                        </td>
                                    </tr>
                                </table>       
                            </div>

                            <div class="grid_6">

                                <table class="app-detail" cellspacing="0">
                                    <tr class="odd">
                                        <c:choose>
                                            <c:when test="${appliesAsRep == true}">
                                                <%-- office applies for a user --%>
                                                <td>
                                                    <b><spring:message code="name" /></b>
                                                </td>
                                                <td>
                                                    <select class="form-select" onchange="window.location.href=this.options
                                                        [this.selectedIndex].value">
                                                        <option value="${formUrlPrefix}/${person.id}/application/new" selected="selected"><c:out value="${person.firstName}" />&nbsp;<c:out value="${person.lastName}" /></option>
                                                        <c:forEach items="${personList}" var="p">
                                                            <c:if test="${person.id != p.id}">
                                                                <option value="${formUrlPrefix}/${p.id}/application/new"><c:out value="${p.firstName}" />&nbsp;<c:out value="${p.lastName}" /></option>
                                                            </c:if>
                                                        </c:forEach>
                                                    </select>
                                                </td> 
                                            </c:when>
                                            <c:otherwise>
                                                <%-- applying for himself/herself --%>
                                                <td style="height:47px;"><b><c:out value="${person.firstName} ${person.lastName}" /></b></td>
                                                <td><c:out value="${person.email}" /></td>
                                            </c:otherwise>
                                        </c:choose>
                                    </tr>
                                    <%@include file="./include/account_days_for_app_view.jsp" %>
                                </table>

                            </div>

                        </form:form>  
                    </c:otherwise>
                </c:choose>



            </div> <!-- End of grid container -->

        </div>

    </body>

</html>
