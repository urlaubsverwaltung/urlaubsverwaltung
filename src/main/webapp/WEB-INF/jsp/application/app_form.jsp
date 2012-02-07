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


<!DOCTYPE html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<spring:url value='/jquery/css/ui-lightness/jquery-ui-1.8.16.custom.css' />" />
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/fluid_grid.css' />" />
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" /> 
        <title><spring:message code="title" /></title>
        <script src="<spring:url value='/jquery/js/jquery-1.6.2.min.js' />" type="text/javascript" ></script>
        <script src="<spring:url value='/jquery/js/jquery-ui-1.8.16.custom.min.js' />" type="text/javascript" ></script>
        <script type="text/javascript">
            $(function() {
                $.datepicker.regional['en'] = {
                    closeText: 'Done',
                    prevText: 'Prev',
                    nextText: 'Next',
                    currentText: 'Today',
                    monthNames: ['January','February','March','April','May','June',
                        'July','August','September','October','November','December'],
                    monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                        'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
                    dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
                    dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
                    dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'],
                    weekHeader: 'Wk',
                    dateFormat: 'dd/mm/yy',
                    firstDay: 1,
                    isRTL: false,
                    showMonthAfterYear: false,
                    yearSuffix: ''};  
                $.datepicker.regional['de'] = {
                    closeText: 'schließen',
                    prevText: 'Zurück',
                    nextText: 'Vor',
                    currentText: 'heute',
                    monthNames: ['Januar','Februar','März','April','Mai','Juni',
                        'Juli','August','September','Oktober','November','Dezember'],
                    monthNamesShort: ['Jan','Feb','Mär','Apr','Mai','Jun',
                        'Jul','Aug','Sep','Okt','Nov','Dez'],
                    dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
                    dayNamesShort: ['So','Mo','Di','Mi','Do','Fr','Sa'],
                    dayNamesMin: ['So','Mo','Di','Mi','Do','Fr','Sa'],
                    weekHeader: 'Wo',
                    dateFormat: 'dd.mm.yy',
                    firstDay: 1,
                    isRTL: false,
                    showMonthAfterYear: false,
                    yearSuffix: ''};  
                $.datepicker.setDefaults($.datepicker.regional["${pageContext.request.locale.language}"]);
            });
        </script>           
        <script type="text/javascript">
            $(function() {
                var dates = $( "#from, #to, #at" ).datepicker({
                    minDate: +0,
                    dateFormat: "dd.mm.yy",
                    numberOfMonths: 1,
                    onSelect: function( selectedDate ) {
                        var option = this.id == "from" ? "minDate" : "maxDate";
                        instance = $( this ).data( "datepicker" ),
                        date = $.datepicker.parseDate(
                        instance.settings.dateFormat ||
                            $.datepicker._defaults.dateFormat,
                        selectedDate, instance.settings );
                        dates.not( this ).datepicker( "option", option, date );
                        if(this.id == "from") {
                            $("#to").datepicker("setDate", "#from");
                        }
                    }
                });
            });
        </script>
        <c:if test="${appForm.howLong != null}">
            <script type="text/javascript">
                $(document).ready(function() {
                    
                    var dayLength = "<c:out value='${appForm.howLong}' />";
            
                    if(dayLength.indexOf("FULL") != -1) {
                        $('#full-day').show(); $('#half-day').hide();
                    } 
                
                    if(dayLength.indexOf("MORNING") != -1) {
                        $('#half-day').show(); $('#full-day').hide();
                    }
                
                    if(dayLength.indexOf("NOON") != -1) {
                        $('#half-day').show(); $('#full-day').hide();
                    }
                });
            </script>
        </c:if>
        <script type="text/javascript">
            $(document).ready(function() {
                $('#error-div').show('drop', 500);
            });
        </script>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/header.jsp" %>

        <div id="content">

            <div class="container_12">
                <c:choose>

                    <c:when test="${notpossible == true}">

                        <spring:message code="app.not.possible" />

                    </c:when>

                    <c:otherwise>    

                        <h2><spring:message code="app.title" /></h2>

                        <form:form method="post" action="${formUrlPrefix}/application/new" modelAttribute="appForm"> 

                            <c:if test="${not empty errors}">
                                <div class="grid_6" id="error-div">
                                    <form:errors cssClass="error" />
                                    <c:if test="${days != null}">
                                        <spring:message code="error.days.start" />&nbsp;<c:out value="${days}" />
                                        <spring:message code="error.days.end" />&nbsp;
                                        <c:choose>
                                                <c:when test="${april == 1}">
                                                    <c:out value="${account.vacationDays + account.remainingVacationDays}"/>&nbsp;<spring:message code="days" />
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${account.vacationDays}"/>&nbsp;<spring:message code="days" />
                                                </c:otherwise>
                                        </c:choose>
                                    </c:if>
                                </div>
                            </c:if>
                            
                            <div class="grid_12">&nbsp;</div>
                            
                            <div class="grid_6 app-form">

                                <table style="width:100%">
                                    <tr>
                                        <td>
                                            <spring:message code="name" />:&nbsp;
                                        </td>
                                        <td class="tbl-right">
                                            <c:out value="${person.lastName}" />,&nbsp;<c:out value="${person.firstName}" />
                                        </td> 
                                    </tr>
                                    <tr>
                                        <td><spring:message code="overview.left" />:</td>
                                        <td class="tbl-right">
                                            <c:choose>
                                                <c:when test="${april == 1}">
                                                    <c:out value="${account.vacationDays + account.remainingVacationDays}"/>&nbsp;<spring:message code="days" />
                                                    &nbsp;(<spring:message code="davon" />&nbsp;<c:out value="${account.remainingVacationDays}"/>
                                                    <spring:message code="days" />&nbsp;<spring:message code="remaining" />)
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${account.vacationDays}"/>&nbsp;<spring:message code="days" />
                                                </c:otherwise>
                                            </c:choose>
                                        </td>    
                                    <tr>
                                        <td colspan="2">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <spring:message code="app.apply" />
                                        </td>
                                        <td class="tbl-right">
                                            <form:select path="vacationType" size="1">
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
                                    </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">
                                            <form:radiobutton path="howLong" checked="checked" value="${full}" onclick="$('#full-day').show(); $('#half-day').hide();" /><spring:message code='${full.dayLength}' /> 
                                            <form:radiobutton path="howLong" value="${morning}" onclick="$('#full-day').hide(); $('#half-day').show();" /><spring:message code='${morning.dayLength}' />
                                            <form:radiobutton path="howLong" value="${noon}" onclick="$('#full-day').hide(); $('#half-day').show();" /><spring:message code='${noon.dayLength}' />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">&nbsp;</td>
                                    </tr>
                                    <tr id="full-day">  
                                        <td>
                                            Von: <form:input id="from" path="startDate" cssErrorClass="error" />
                                        <br />
                                        <form:errors path="startDate" cssClass="error" cssStyle="padding-left: 2.45em;" />
                                        </td>
                                        <td id="date-to">
                                            Bis: <form:input id="to" path="endDate" cssErrorClass="error" />
                                        <br />
                                        <form:errors path="endDate" cssClass="error" cssStyle="padding-left: 2em;" />
                                        </td>
                                    </tr>
                                    <tr id="half-day" style="display: none">  
                                        <td>
                                            Am: <form:input id="at" path="startDateHalf" cssErrorClass="error" />
                                        <br />
                                        <form:errors path="startDateHalf" cssClass="error" cssStyle="padding-left: 2.45em;" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label for="reason"><spring:message code='reason' />:</label>
                                        </td>
                                        <td class="tbl-right">
                                            <form:input id="reason" path="reason" cssErrorClass="error" />
                                        <br />
                                        <form:errors path="reason" cssClass="error" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label for="vertreter"><spring:message code='app.rep' />:</label> 
                                        </td>
                                        <td class="tbl-right">
                                            <form:select path="rep" id="vertreter" size="1">
                                        <option value="<spring:message code='app.no.rep' />"><spring:message code='app.no.rep' /></option>
                                        <c:forEach items="${persons}" var="einmitarbeiter">
                                            <option value="${einmitarbeiter.lastName} ${einmitarbeiter.firstName}">
                                                <c:out value='${einmitarbeiter.lastName}' />&nbsp;<c:out value="${einmitarbeiter.firstName}" />
                                            </option>
                                        </c:forEach>
                                    </form:select>                             
                                    </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label for="anschrift"><spring:message code='app.address' />:</label>
                                        </td>
                                        <td class="tbl-right">
                                            <form:input id="anschrift" path="address" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <label for="telefon"><spring:message code='app.phone' />:</label>
                                        </td>
                                        <td class="tbl-right">
                                            <form:input id="telefon" path="phone" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">&nbsp;</td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">
                                            <spring:message code='app.footer' />&nbsp;<joda:format style="M-" value="${date}"/>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                            <div class="grid_12">&nbsp;</div>
                            <div class="grid_12">

                                <input class="confirm"type="submit" name="<spring:message code='apply' />" value="<spring:message code='apply' />" />

                            </div>

                        </form:form>  
                    </c:otherwise>
                </c:choose>

            </div>
        </div>

    </body>

</html>
