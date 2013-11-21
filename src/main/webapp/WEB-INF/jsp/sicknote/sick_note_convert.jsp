<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<html>
<head>
    <title><spring:message code="title"/></title>
    <%@include file="../include/header.jsp" %>

    <script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>

    <script type="text/javascript">
        $(document).ready(function() {

            var regional = "${pageContext.request.locale.language}";

            $.datepicker.setDefaults($.datepicker.regional[regional]);

            $("#from, #to").datepicker({
                numberOfMonths: 1,
                onSelect: function(date) {
                    if (this.id == "from") {
                        $("#to").datepicker("setDate", date);
                    }
                },
                beforeShowDay: function (date) {

                    // if day is saturday or sunday, highlight it
                    if (date.getDay() == 6 || date.getDay() == 0) {
                        return [true, "notworkday"];
                    } else {
                        return [true, ""];
                    }

                }
            });

        });
        
    </script>
    
    <style type="text/css">
        form.form-horizontal .control-label {
            width: 20em !important;
        }

        form.form-horizontal input[type="text"] {
            height: 28px !important; 
        }
    </style>
    
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
                        <p>
                            Krankmeldung in Urlaub umwandeln
                        </p>
                    </legend>
                </div>
    
                <c:set var="METHOD" value="POST" />
                <c:set var="ACTION" value="${formUrlPrefix}/sicknote/${sickNote.id}/convert" />
                
                <form:form method="${METHOD}" action="${ACTION}" modelAttribute="appForm" class="form-horizontal">
    
                    <div class="control-group">
                        <label class="control-label"><spring:message code='staff'/></label>
    
                        <div class="controls">
                            <c:out value="${sickNote.person.firstName}" />&nbsp;<c:out value="${sickNote.person.lastName}" />
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">Urlaubstyp</label>

                        <div class="controls">
                            <form:select path="vacationType" size="1" class="input-medium">
                                <c:forEach items="${vacTypes}" var="vacType">
                                    <option value="${vacType}">
                                        <spring:message code='${vacType.vacationTypeName}' />
                                    </option>
                                </c:forEach>
                            </form:select>
                        </div>
                    </div>
    
                    <div class="control-group">
                        <label class="control-label" for="from"><spring:message code="time" /></label>
    
                        <div class="controls">
                            <spring:message code="From" />
                            <br />
                            <form:input id="from" path="startDate" cssClass="input-medium" cssErrorClass="error input-medium" />
                            <span class="help-inline"><form:errors path="startDate" cssClass="error"/></span>
                        </div>
                    </div>
    
                    <div class="control-group">
                        <label class="control-label" for="to">&nbsp;</label>
    
                        <div class="controls">
                            <spring:message code="To" />
                            <br />
                            <form:input id="to" path="endDate" cssClass="input-medium" cssErrorClass="error input-medium" />
                            <span class="help-inline"><form:errors path="endDate" cssClass="error"/></span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">
                            <label class="control-label" for="reason"><spring:message code="reason" /></label>
                        </label>

                        <div class="controls">
                            <span id="count-chars"></span><spring:message code="max.chars" />
                            <br />
                            <form:textarea id="reason" path="reason" cssErrorClass="error" rows="4" onkeyup="count(this.value, 'count-chars');" onkeydown="maxChars(this,200); count(this.value, 'count-chars');" />
                        </div>
                    </div>

                    <hr/>
                    
                    <div class="control-group">
                        <button class="btn" type="submit"><i class='icon-ok'></i>&nbsp;<spring:message code="save" /></button>
                        <a class="btn" href="${formUrlPrefix}/sicknote/${sickNote.id}"><i class='icon-remove'></i>&nbsp;<spring:message code='cancel'/></a>
                    </div>
                
                </form:form>
                
            </div>

            <div class="grid_6">

                <div class="overview-header">
                    <legend>
                        <p><spring:message code="sicknote" /></p>
                    </legend>
                </div>

                <table class="app-detail">
                    <tbody>
                    <tr class="odd">
                        <td><spring:message code="name" /></td>
                        <td><c:out value="${sickNote.person.firstName}" />&nbsp;<c:out value="${sickNote.person.lastName}" /></td>
                    </tr>
                    <tr class="even">
                        <td><spring:message code="sicknotes.time" /></td>
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
                        <td><spring:message code="sicknotes.aub.time" /></td>
                        <td><joda:format style="M-" value="${sickNote.aubStartDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${sickNote.aubEndDate}"/></td>
                    </tr>
                    </tbody>
                </table>
            </div>
            
        </div>
        
    </div>
    
</div>    

</body>
</html>