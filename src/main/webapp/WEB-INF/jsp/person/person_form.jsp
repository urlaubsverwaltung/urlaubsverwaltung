<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="person" tagdir="/WEB-INF/tags/person" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head />
    <script type="text/javascript">
        $(document).ready(function () {
            $('#error-div').show('drop', 500);
        });
    </script>
    <script type="text/javascript">
        function change(year) {
            var url = "?year=" + year;
            window.location.href = url;
        }
    </script>
    <script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>
    <script type="text/javascript">
        $(document).ready(function() {
            var regional = "${pageContext.request.locale.language}";
            $.datepicker.setDefaults($.datepicker.regional[regional]);
            $("#validFrom").datepicker();
        });
    </script>
</head>

<body>

<uv:menu />

<spring:url var="formUrlPrefix" value="/web"/>


<div id="content">
<div class="container_12">

<c:choose>
    <c:when test="${person.id == null}">
        <c:set var="METHOD" value="POST"/>
        <c:set var="ACTION" value="${formUrlPrefix}/staff/new"/>
    </c:when>
    <c:otherwise>
        <c:set var="METHOD" value="PUT"/>
        <c:set var="ACTION" value="${formUrlPrefix}/staff/${person.id}/edit"/>
    </c:otherwise>
</c:choose>

<form:form method="${METHOD}" action="${ACTION}" modelAttribute="personForm" class="form-horizontal">

<div class="grid_5 person-data">

    <div class="overview-header">

        <legend>
            <p>
                <spring:message code="person.data"/>
            </p>
        </legend>

    </div>
    
    <div class="control-group">
        <label class="control-label" for="login"><spring:message code='login'/></label>

        <div class="controls">
            <c:choose>
                <c:when test="${person.id == null}">
                    <form:input id="login" path="loginName" cssErrorClass="error"/>
                    <span class="help-inline"><form:errors path="loginName" cssClass="error"/></span>
                </c:when>
                <c:otherwise>
                    <c:out value="${person.loginName}"/>
                    <form:hidden path="loginName" value="${person.loginName}"/>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="firstName"><spring:message code='firstname'/></label>

        <div class="controls">
            <form:input id="firstName" path="firstName" cssErrorClass="error"/>
            <span class="help-inline"><form:errors path="firstName" cssClass="error"/></span>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="lastName"><spring:message code='lastname'/></label>

        <div class="controls">
            <form:input id="lastName" path="lastName" cssErrorClass="error"/>
            <span class="help-inline"><form:errors path="lastName" cssClass="error"/></span>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="email"><spring:message code='email'/></label>

        <div class="controls">
            <form:input id="email" path="email" cssErrorClass="error"/>
            <span class="help-inline"><form:errors path="email" cssClass="error"/></span>
        </div>
    </div>
    
    <c:if test="${fn:length(workingTimes) > 1}">

    <div class="control-group">
        <label class="control-label"><spring:message code='working.times'/></label>

        <div class="controls">
            <c:forEach items="${workingTimes}" var="time">
                <spring:message code="working.time.valid.from" />
                <uv:date date="${time.validFrom}" />
                <br />
                <c:if test="${time.monday.duration > 0}">
                    <spring:message code="monday" />
                </c:if>
                <c:if test="${time.tuesday.duration > 0}">
                    <spring:message code="tuesday" />
                </c:if>
                <c:if test="${time.wednesday.duration > 0}">
                    <spring:message code="wednesday" />
                </c:if>
                <c:if test="${time.thursday.duration > 0}">
                    <spring:message code="thursday" />
                </c:if>
                <c:if test="${time.friday.duration > 0}">
                    <spring:message code="friday" />
                </c:if>
                <c:if test="${time.saturday.duration > 0}">
                    <spring:message code="saturday" />
                </c:if>
                <c:if test="${time.sunday.duration > 0}">
                    <spring:message code="sunday" />
                </c:if>
                <br />
                <br />
            </c:forEach>
        </div>
    </div>

    </c:if>

    <div class="control-group">
        <label class="control-label">
            <spring:message code='working.time'/>
            <br />
            <form:errors path="validFrom" cssClass="error" />
        </label>

        <div class="controls">

            <spring:message code="working.time.valid.from" />&nbsp;
            <c:set var="VALID_FROM"><uv:date date="${personForm.validFrom}" /></c:set>
            <form:input id="validFrom" path="validFrom" value="${VALID_FROM}" cssErrorClass="error input-medium" cssClass="input-medium" />

            <c:forEach items="${weekDays}" var="weekDay">
                <label class="checkbox" for="${weekDay.name}">
                    <form:checkbox id="${weekDay.name}" path="workingDays" value="${weekDay.dayOfWeek}" />
                    &nbsp;<spring:message code='${weekDay.name}'/>
                </label> 
            </c:forEach>
            
        </div>
    </div>

</div>


<div class="grid_7 vacation-data">

    <div class="overview-header">

        <legend>
            <p>
                <spring:message code="entitlement"/>
            </p>
        </legend>

    </div>
    
    <div class="control-group">
        <label class="control-label" for="year-dropdown"><spring:message code='year'/></label>

        <div class="controls">

            <c:choose>
                <c:when test="${person.id == null}">
                    <form:select path="year" size="1" id="year-dropdown">
                        <form:option value="${currentYear - 1}"><c:out value="${currentYear - 1}"/></form:option>
                        <form:option value="${currentYear}" selected="selected"><c:out
                                value="${currentYear}"/></form:option>
                        <form:option value="${currentYear + 1}"><c:out value="${currentYear + 1}"/></form:option>
                        <form:option value="${currentYear + 2}"><c:out value="${currentYear + 2}"/></form:option>
                    </form:select>
                </c:when>
                <c:otherwise>
                    <form:select path="year" size="1" onchange="change(this.options[this.selectedIndex].value);"
                                 id="year-dropdown">
                        <form:option value="${currentYear - 1}"><c:out value="${currentYear - 1}"/></form:option>
                        <form:option value="${currentYear}"><c:out value="${currentYear}"/></form:option>
                        <form:option value="${currentYear + 1}"><c:out value="${currentYear + 1}"/></form:option>
                        <form:option value="${currentYear + 2}"><c:out value="${currentYear + 2}"/></form:option>
                    </form:select>
                </c:otherwise>
            </c:choose>

            <span class="help-inline"><form:errors path="year" cssClass="error"/></span>

        </div>
    </div>

    <div class="control-group">
        <label class="control-label">
            <spring:message code='time'/>
            <c:if test="${not empty errors}">
                <br />
                <span><form:errors cssClass="error"/></span>
            </c:if>
        </label>

        <div class="controls">
            
            <person:day-dropdown path="dayFrom" selected="${personForm.dayFrom}" />
            <person:month-dropdown path="monthFrom" selected="${personForm.monthFrom}" />
            
            <spring:message code='to'/>

            <person:day-dropdown path="dayTo" selected="${personForm.dayTo}" />
            <person:month-dropdown path="monthTo" selected="${personForm.monthTo}" />
            
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="annualVacationDays"><spring:message code='person.annual.vacation'/></label>

        <div class="controls">
            <form:input path="annualVacationDays" cssErrorClass="error" size="1" id="annualVacationDays"/>
            <span class="help-inline"><form:errors path="annualVacationDays" cssClass="error"/></span>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="remainingVacationDays">
            <spring:message code="remaining"/>&nbsp;<spring:message code="last.year"/>
        </label>

        <div class="controls">
            <form:input path="remainingVacationDays" cssErrorClass="error" size="1" id="remainingVacationDays"/>
            <span class="help-inline"><form:errors path="remainingVacationDays" cssClass="error"/></span>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label">
            <spring:message code='person.expire'/>
        </label>

        <div class="controls" style="padding-top: 5px;">
            <spring:message code='yes'/>&nbsp;<form:radiobutton path="remainingVacationDaysExpire" value="true"/>
            &nbsp;&nbsp;&nbsp;
            <spring:message code='no'/>&nbsp;<form:radiobutton path="remainingVacationDaysExpire" value="false"/>
        </div>
    </div>

</div>

<div class="grid_12">
    <hr/>
</div>

<div class="grid_12">

    <button class="btn" type="submit"><i class='icon-ok'></i>&nbsp;<spring:message code="save" /></button>
    <a class="btn" href="${formUrlPrefix}/staff"><i class='icon-remove'></i>&nbsp;<spring:message code='cancel'/></a>

</div>

</form:form>

</div>
</div>

</body>

</html>
