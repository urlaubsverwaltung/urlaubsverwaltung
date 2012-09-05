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
        <%@include file="../include/header.jsp" %>
        <title><spring:message code="title" /></title>
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
                    dateFormat: 'dd.mm.yy',
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
                var dates = $( "#from, #to, #at" ).datepicker({
                    numberOfMonths: 1,
                    onSelect: function( selectedDate ) {
                        //                        var option = this.id == "from" ? "minDate" : "maxDate";
                        instance = $( this ).data( "datepicker" ),
                        date = $.datepicker.parseDate(
                        instance.settings.dateFormat ||
                            $.datepicker._defaults.dateFormat,
                        selectedDate, instance.settings );
                        //                        dates.not( this ).datepicker( "option", option, date );
                        //                        if(this.id == "from") {
                        //                            $("#to").datepicker("setDate", "#from");
                        //                        }
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
                $('#error-div').show();
            });
        </script>
        <style type="text/css">
            .app-detail td {
                width:auto;
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

                        <div class="grid_6">

                            <table class="overview-header" style="margin-bottom:1em">
                                <tr>
                                    <td><spring:message code="app.title" /></td>
                                </tr>
                            </table>
                        </div>

                        <c:choose>
                            <c:when test="${setForce != null}">
                                <c:set var="forcy" value="${setForce}" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="forcy" value="0" />
                            </c:otherwise>
                        </c:choose>
                        <form:form method="post" action="${formUrlPrefix}/application/new?force=${forcy}" modelAttribute="appForm"> 

                            <div class="grid_12">&nbsp;</div>

                            <div class="grid_6">

                                <table class="app-detail" cellspacing="0">
                                    <tr class="odd">
                                        <th><c:out value="${person.firstName} ${person.lastName}" /></th>
                                        <td><c:out value="${person.email}" /></td>
                                    </tr>
                                    <tr class="even">
                                        <td><spring:message code="overview.left" /></td>
                                        <td>
                                            <c:choose>

                                                <c:when test="${account != null}">
                                                    <c:set var ="left" value="${leftDays}" />
                                                    <c:choose>
                                                        <c:when test="${left <= 1.00 && left > 0.50}">
                                                            <c:set var="numberOfDays" value="day" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:set var="numberOfDays" value="days" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <spring:message code="${numberOfDays}" arguments="${left}" />
                                                </c:when>

                                                <c:otherwise>
                                                    <spring:message code='not.specified' />
                                                </c:otherwise> 

                                            </c:choose>
                                        </td>
                                    </tr>
                                </table>

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

                                <table class="app-detail tbl-margin-top" cellspacing="0">
                                    <tr class="odd">
                                        <th>
                                            <spring:message code="app.apply" />
                                        </th>
                                        <td>
                                            <form:select path="vacationType" size="1" style="width:98%;">
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
                                    <tr class="even">
                                        <td>
                                            <form:radiobutton path="howLong" checked="checked" value="${full}" onclick="$('#full-day').show(); $('#half-day').hide();" /><spring:message code='${full.dayLength}' /> 
                                            <form:radiobutton path="howLong" value="${morning}" onclick="$('#full-day').hide(); $('#half-day').show();" /><spring:message code='${morning.dayLength}' />
                                            <form:radiobutton path="howLong" value="${noon}" onclick="$('#full-day').hide(); $('#half-day').show();" /><spring:message code='${noon.dayLength}' />
                                        </td>
                                        <td>
                                            <span id="full-day">
                                                Von: <form:input id="from" path="startDate" cssErrorClass="error" size="4" />
                                                <%-- <form:errors path="startDate" cssClass="error" cssStyle="padding-left: 2.45em;" /> --%>
                                                &nbsp;
                                                Bis: <form:input id="to" path="endDate" cssErrorClass="error" size="4" />
                                                <%--<form:errors path="endDate" cssClass="error" cssStyle="padding-left: 2em;" />--%>
                                            </span>

                                            <span id="half-day" style="display: none">
                                                Am: <form:input id="at" path="startDateHalf" cssErrorClass="error" size="4" />
                                                <br />
                                                <%--<form:errors path="startDateHalf" cssClass="error" cssStyle="padding-left: 2.45em;" />--%>
                                            </span>
                                        </td>
                                    </tr>
                                    <tr class="odd">
                                        <td>
                                            <label for="reason"><spring:message code='reason' />&nbsp;<spring:message code='app.reason.describe' /></label>
                                        </td>
                                        <td>
                                            <form:input id="reason" path="reason" cssErrorClass="error" size="22" />
                                            <br />
                                            <form:errors path="reason" cssClass="error" />
                                        </td>
                                    </tr>
                                    <tr class="even">
                                        <td>
                                            <label for="vertreter"><spring:message code='app.rep' />:</label> 
                                        </td>
                                        <td>
                                            <form:select path="rep" id="vertreter" size="1" style="width:98%;">
                                        <option value="<spring:message code='app.no.rep' />"><spring:message code='app.no.rep' /></option>
                                        <c:forEach items="${persons}" var="einmitarbeiter">
                                            <option value="${einmitarbeiter.lastName} ${einmitarbeiter.firstName}">
                                                <c:out value="${einmitarbeiter.firstName}" />&nbsp;<c:out value='${einmitarbeiter.lastName}' />
                                            </option>
                                        </c:forEach>
                                    </form:select>                             
                                    </td>
                                    </tr>
                                    <tr class="odd">
                                        <td>
                                            <label for="anschrift"><spring:message code='app.address' />:</label>
                                        </td>
                                        <td>
                                            <form:input id="anschrift" path="address" size="22" />
                                        </td>
                                    </tr>
                                    <tr class="even">
                                        <td>
                                            <label for="telefon"><spring:message code='app.phone' />:</label>
                                        </td>
                                        <td>
                                            <form:input id="telefon" path="phone" size="22" />
                                        </td>
                                    </tr>
                                </table>
                                <table class="app-detail tbl-margin-top" cellspacing="0">
                                    <tr class="odd">
                                        <td>
                                            <spring:message code='app.footer' />&nbsp;<joda:format style="M-" value="${date}"/>
                                        </td>
                                        <td style="text-align:right;padding-right:1.9em;">
                                            <input class="confirm"type="submit" name="<spring:message code='apply' />" value="<spring:message code='apply' />" />
                                        </td>
                                    </tr>
                                </table>       
                            </div>
                            <div class="grid_12">&nbsp;</div>

                        </form:form>  
                    </c:otherwise>
                </c:choose>

            </div>
        </div>

    </body>

</html>
