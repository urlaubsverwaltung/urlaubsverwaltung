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
    </head>

    <body>

        <%@include file="../include/menu_header.jsp" %>

        <spring:url var="formUrlPrefix" value="/web" />


        <div id="content">
            <div class="container_12">

                <form:form method="put" action="${formUrlPrefix}/staff/${person.id}/edit" modelAttribute="personForm"> 
                    <c:if test="${not empty errors}">
                        <div class="grid_6" id="error-div"><form:errors cssClass="error" /></div>
                    </c:if>   

                    <div class="grid_12">&nbsp;</div>

                    <div class="grid_8">
                        <table id="person-form-tbl">
                            <tr>
                                <th style="padding-bottom: 0.5em;"><spring:message code='person.data' /></th>
                                <td colspan="2">&nbsp;</td>
                            </tr>
                            <tr>
                                <td><spring:message code='login' />:</td>
                                <td><c:out value="${person.loginName}" /></td>
                            </tr>
                            <tr>
                                <td><label for="vorname"><spring:message code="firstname" />:</label></td>
                                <td>
                                    <form:input id="vorname" path="firstName" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="firstName" cssClass="error" />
                                </td>
                            </tr>
                            <tr>
                                <td><label for="nachname"><spring:message code="lastname" />:</label></td>
                                <td>
                                    <form:input id="nachname" path="lastName" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="lastName" cssClass="error" />
                                </td>
                            </tr>
                            <tr>
                                <td><label for="email"><spring:message code="email" />:</label></td>
                                <td>
                                    <form:input id="email" path="email" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="email" cssClass="error" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">&nbsp;</td>
                            </tr>
                            <tr>
                                <th>
                                    <spring:message code='person.entitlement' />
                                </th>
                                <td>
                                    <form:select path="year" size="1" onchange="change(this.options[this.selectedIndex].value);">
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
                            <tr>
                                <td><spring:message code="person.annual.vacation" />:</td>
                                <td>
                                    <form:input path="annualVacationDaysEnt" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="annualVacationDaysEnt" cssClass="error" />
                                </td>
                            </tr>
                            <tr>
                                <td><spring:message code="person.vac" />:</td>
                                <td>
                                    <form:input path="vacationDaysEnt" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="vacationDaysEnt" cssClass="error" />
                                </td>
                            </tr>
                            <tr>   
                                <td>
                                    <spring:message code="remaining" />&nbsp;<spring:message code="last.year" />:
                                </td>
                                <td>
                                    <form:input path="remainingVacationDaysEnt" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="remainingVacationDaysEnt" cssClass="error" /> 
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">&nbsp;</td>
                            </tr>
                            <tr>
                                <th>
                                    <spring:message code='person.account' />
                                </th>
                            </tr>    
                            <tr>
                                <td>
                                    <spring:message code='overview.left' />:
                                </td>
                                <td>
                                    <form:input path="vacationDaysAcc" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="vacationDaysAcc" cssClass="error" /> 
                                </td>    
                            </tr>  
                            <tr>
                                <td>
                                    <spring:message code='overview.left.remaining' />:
                                </td>
                                <td>
                                    <form:input path="remainingVacationDaysAcc" cssErrorClass="error" />
                                </td>
                                <td>
                                    <form:errors path="remainingVacationDaysAcc" cssClass="error" /> 
                                </td> 
                            </tr>
                            <tr>
                                <td>
                                    <spring:message code='person.expire' />
                                </td>
                                <td>
                                    <spring:message code='yes' /> <form:radiobutton path="remainingVacationDaysExpireAcc" value="true"  />
                                    &nbsp;&nbsp;&nbsp;  
                                    <spring:message code='no' /> <form:radiobutton path="remainingVacationDaysExpireAcc" value="false" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">&nbsp;</td>
                            </tr>
                        </table>

                                <br />
                                    <input type="submit" class="save" name="<spring:message code="save" />" value="<spring:message code="save" />" />
                                    <a class="button back" href="${formUrlPrefix}/staff"><spring:message code='cancel' /></a>
                                    <input type="button" onclick="$('#activ-action').show();"
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
                        <div id="activ-action"style="display: none;">
                            <c:choose>
                                <c:when test="${person.active == true}">
                                    <spring:message code='person.deactivate.confirm' />&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <spring:message code='person.activate.confirm' />&nbsp;
                                </c:otherwise> 
                            </c:choose>       
                            <input type="submit" name="<spring:message code="yes" />" value="<spring:message code="yes" />" />
                            <input type="button" onclick="$('#activ-action').hide();" name="<spring:message code="no" />" value="<spring:message code="no" />" /> 
                        </div>         
                    </form:form>

                </div>

                    
            </div> 
        </div>    

    </body>

</html>
