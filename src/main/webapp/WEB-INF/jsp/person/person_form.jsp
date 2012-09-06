<%-- 
    Document   : person_form
    Created on : 31.10.2011, 10:00:10
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
        <title><spring:message code="title" /></title>
        <%@include file="../include/header.jsp" %>
        <script type="text/javascript">
            $(document).ready(function() {
                $('#error-div').show('drop', 500);
            });
        </script>
        <script type="text/javascript">
            function change(year) {
                var url = "?year=" + year;
                window.location.href= url;
            }
        </script>
        <style type="text/css">
            .td-name {
                width: 40%;
            }
        </style>
    </head>

    <body>

        <%@include file="../include/menu_header.jsp" %>

        <spring:url var="formUrlPrefix" value="/web" />


        <div id="content">
            <div class="container_12">

                <form:form method="put" action="${formUrlPrefix}/staff/${person.id}/edit" modelAttribute="personForm"> 
                    <c:if test="${not empty errors}">
                        <div class="grid_8" id="error-div"><form:errors cssClass="error" /></div>
                    </c:if>   

                    <div class="grid_8">
                        <table class="overview-header" style="margin-bottom:1em">
                            <tbody>
                                <tr>
                                    <td><spring:message code='person.data' /></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="grid_8">
                        <table class="app-detail" cellspacing="0">
                            <tr class="odd">
                                <td class="td-name"><spring:message code='login' />:</td>
                                <td><c:out value="${person.loginName}" /></td>
                                <td>&nbsp;</td>
                            </tr>
                            <tr class="even">
                                <td class="td-name"><label for="vorname"><spring:message code="firstname" />:</label></td>
                                <td>
                                    <form:input id="vorname" path="firstName" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="firstName" cssClass="error" />
                                </td>
                            </tr>
                            <tr class="odd">
                                <td class="td-name"><label for="nachname"><spring:message code="lastname" />:</label></td>
                                <td>
                                    <form:input id="nachname" path="lastName" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="lastName" cssClass="error" />
                                </td>
                            </tr>
                            <tr class="even">
                                <td class="td-name"><label for="email"><spring:message code="email" />:</label></td>
                                <td>
                                    <form:input id="email" path="email" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="email" cssClass="error" />
                                </td>
                            </tr>
                        </table>
                    </div>

                    <div class="grid_12">&nbsp;</div>

                    <div class="grid_8">
                        <table class="overview-header" style="margin-bottom:1em">
                            <tbody>
                                <tr>
                                    <td><spring:message code='entitlement' /></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="grid_8">
                        <table class="app-detail" cellspacing="0">

                            <tr class="odd">
                                <td class="td-name"><spring:message code='year' /></td>
                                <td>
                                    <form:select path="year" size="1" onchange="change(this.options[this.selectedIndex].value);" id="year-dropdown">
                                        <form:option value="${currentYear - 1}"><c:out value="${currentYear - 1}" /></form:option>
                                        <form:option value="${currentYear}"><c:out value="${currentYear}" /></form:option>
                                        <form:option value="${currentYear + 1}"><c:out value="${currentYear + 1}" /></form:option>
                                        <form:option value="${currentYear + 2}"><c:out value="${currentYear + 2}" /></form:option>
                                    </form:select>
                                </td>
                                <td>
                                    <form:errors path="year" cssClass="error" />
                                </td>
                            </tr>

                            <tr class="even">
                                <td class="td-name"><spring:message code='time' /></td>
                                <td colspan="2">
                                    <form:select path="dayFrom" size="1">
                                        <%--<form:option value="0" disabled="disabled"><spring:message code='person.edit.day' /></form:option>--%>
                                        <script type="text/javascript">
                                            var i = 1;
                                            for(i = 1; i < 32; i++) {
                                                if(<c:out value="${personForm.dayFrom}" /> == i) {
                                                    document.write('<form:option selected="selected" value="' + i + '">' + i + '</form:option>');
                                                } else {
                                                    document.write('<form:option value="' + i + '">' + i + '</form:option>');
                                                }
                                            }
                                        </script>
                                    </form:select>
                                    <form:select path="monthFrom" size="1">
                                        <%--<form:option value="0" disabled="disabled"><spring:message code='person.edit.month' /></form:option>--%>
                                        <script type="text/javascript">
                                            var regional = "${pageContext.request.locale.language}";
                                            var monthNames;
                
                                            if(regional == "en") {
                                                monthNames = ['January','February','March','April','May','June',
                                                    'July','August','September','October','November','December'];
                                            } else {
                                                // default = german
                                                monthNames = ['Januar','Februar','März','April','Mai','Juni',
                                                    'Juli','August','September','Oktober','November','Dezember'];
                                            }
                        
                                            var i = 1;
                                            for(i = 1; i < 13; i++) {
                                                if(<c:out value="${personForm.monthFrom}" /> == i) {
                                                    document.write('<form:option selected="selected" value="' + i + '">' + monthNames[i-1] + '</form:option>');
                                                } else {
                                                    document.write('<form:option value="' + i + '">' + monthNames[i-1] + '</form:option>');
                                                }
                                            }
                                        </script>
                                    </form:select>

                                    <spring:message code='to' />
                                    <form:select path="dayTo" size="1">
                                        <%--<form:option value="0" selected="selected" disabled="disabled"><spring:message code='person.edit.day' /></form:option>--%>
                                        <script type="text/javascript">
                                            var i = 1;
                                            for(i = 1; i < 32; i++) {
                                                if(<c:out value="${personForm.dayTo}" /> == i) {
                                                    document.write('<form:option selected="selected" value="' + i + '">' + i + '</form:option>');
                                                } else {
                                                    document.write('<form:option value="' + i + '">' + i + '</form:option>');
                                                }
                                            }
                                        </script>
                                    </form:select>
                                    <form:select path="monthTo" size="1">
                                        <%--<form:option value="0" selected="selected" disabled="disabled"><spring:message code='person.edit.month' /></form:option>--%>
                                        <script type="text/javascript">
                                            var regional = "${pageContext.request.locale.language}";
                                            var monthNames;
                
                                            if(regional == "en") {
                                                monthNames = ['January','February','March','April','May','June',
                                                    'July','August','September','October','November','December'];
                                            } else {
                                                // default = german
                                                monthNames = ['Januar','Februar','März','April','Mai','Juni',
                                                    'Juli','August','September','Oktober','November','Dezember'];
                                            }
                        
                                            var i = 1;
                                            for(i = 1; i < 13; i++) {        
                                                if(<c:out value="${personForm.monthTo}" /> == i) {
                                                    document.write('<form:option selected="selected" value="' + i + '">' + monthNames[i-1] + '</form:option>');
                                                } else {
                                                    document.write('<form:option value="' + i + '">' + monthNames[i-1] + '</form:option>');
                                                }
                                            }
                                        </script>
                                    </form:select>
                                </td>
                            </tr>
                            <tr class="odd">
                                <td class="td-name"><spring:message code="person.annual.vacation" />:</td>
                                <td>
                                    <form:input path="annualVacationDays" cssErrorClass="error" size="1" />
                                </td>
                                <td>
                                    <form:errors path="annualVacationDays" cssClass="error" />
                                </td>
                            </tr>
                            <tr class="even">   
                                <td class="td-name">
                                    <spring:message code="remaining" />&nbsp;<spring:message code="last.year" />:
                                </td>
                                <td>
                                    <form:input path="remainingVacationDays" cssErrorClass="error" size="1"  />
                                </td>
                                <td>
                                    <form:errors path="remainingVacationDays" cssClass="error" /> 
                                </td>
                            </tr>
                            <tr class="odd">
                                <td class="td-name">
                                    <spring:message code='person.expire' />
                                </td>
                                <td>
                                    <spring:message code='yes' /> <form:radiobutton path="remainingVacationDaysExpire" value="true"  />
                                    &nbsp;&nbsp;&nbsp;  
                                    <spring:message code='no' /> <form:radiobutton path="remainingVacationDaysExpire" value="false" />
                                </td>
                                <td>&nbsp;</td>
                            </tr>
                        </table>
                    </div>

                    <!--                        <div class="grid_12" style="margin-top: 2em; background-color: #EAF2D3;">-->
                    <div class="grid_12">&nbsp;</div>
                    <div class="grid_8" style="background-color: #EAF2D3; height: 2em; padding-top: 1em; padding-bottom: 1em;">
                        &nbsp;
                        <input class="btn btn-primary" type="submit" name="<spring:message code="save" />" value="<spring:message code="save" />" />
                        <a class="btn" href="${formUrlPrefix}/staff"><spring:message code='cancel' /></a>
                        <input class="btn" type="button" onclick="$('#activ-action').show();"
                               <c:choose>
                                   <c:when test="${person.active == true}">
                                       name="<spring:message code='person.deactivate' />"
                                       value="<spring:message code='person.deactivate' />"
                                       class="deactivate"
                                   </c:when>
                                   <c:otherwise>
                                       name="<spring:message code='person.activate' />"
                                       value="<spring:message code='person.activate' />"
                                   </c:otherwise>
                               </c:choose>    
                               />


                    </form:form>

                    <br />
                    <br />

                    <c:choose>
                        <c:when test="${person.active == true}">
                            <c:set var="formUrl" value="${formUrlPrefix}/staff/${person.id}/deactivate" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="formUrl" value="${formUrlPrefix}/staff/${person.id}/activate" />  
                        </c:otherwise>
                    </c:choose>

                    <form:form method="put" action="${formUrl}">                  
                        <div id="activ-action"style="display: none;" class="confirm-green">
                            <c:choose>
                                <c:when test="${person.active == true}">
                                    <spring:message code='person.deactivate.confirm' />&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <spring:message code='person.activate.confirm' />&nbsp;
                                </c:otherwise> 
                            </c:choose>       
                            <input class="btn" type="submit" name="<spring:message code="yes" />" value="<spring:message code="yes" />" />
                            <input class="btn" type="button" onclick="$('#activ-action').hide();" name="<spring:message code="no" />" value="<spring:message code="no" />" /> 
                        </div>         
                    </form:form>
                </div>



            </div> 
        </div>    

    </body>

</html>
