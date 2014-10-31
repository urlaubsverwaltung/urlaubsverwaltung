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
        function change(year) {
            var url = "?year=" + year;
            window.location.href = url;
        }
    </script>
</head>

<body>

<uv:menu />

<spring:url var="formUrlPrefix" value="/web"/>


<div class="content">
<div class="container">

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

<div class="row">

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend>
            <p>
                <spring:message code="person.data"/>
            </p>
        </legend>

    </div>
    
    <div class="form-group">
        <label class="control-label col-md-4" for="login"><spring:message code='login'/></label>

        <div class="col-md-7">
            <c:choose>
                <c:when test="${person.id == null}">
                    <form:input id="login" path="loginName" class="form-control" cssErrorClass="form-control error"/>
                    <span class="help-inline"><form:errors path="loginName" cssClass="error"/></span>
                </c:when>
                <c:otherwise>
                    <c:out value="${person.loginName}"/>
                    <form:hidden path="loginName" value="${person.loginName}"/>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="firstName"><spring:message code='firstname'/></label>

        <div class="col-md-7">
            <form:input id="firstName" path="firstName" class="form-control" cssErrorClass="form-control error" />
            <span class="help-inline"><form:errors path="firstName" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="lastName"><spring:message code='lastname'/></label>

        <div class="col-md-7">
            <form:input id="lastName" path="lastName" class="form-control" cssErrorClass="form-control error" />
            <span class="help-inline"><form:errors path="lastName" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="email"><spring:message code='email'/></label>

        <div class="col-md-7">
            <form:input id="email" path="email" class="form-control" cssErrorClass="form-control error" />
            <span class="help-inline"><form:errors path="email" cssClass="error"/></span>
        </div>
    </div>
    
</div>

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend>
            <p>
                <spring:message code="role"/>
            </p>
        </legend>

    </div>

    <div class="form-group">

        <div class="col-md-11 checkbox">

            <span class="help-inline"><form:errors path="permissions" cssClass="error"/></span>

            <div class="person--role">
                <label class="checkbox">
                    <form:checkbox path="permissions" value="INACTIVE"/><b><spring:message code="role.inactive"/></b>:
                    <spring:message code="role.inactive.dsc"/>
                </label>
            </div>

            <div class="person--role">
                <label class="checkbox">
                    <form:checkbox path="permissions" value="USER"/><b><spring:message code="role.user"/></b>:
                    <spring:message code="role.user.dsc"/>
                </label>
                <label class="checkbox person--mail-notification">
                    <form:checkbox path="notifications" value="NOTIFICATION_USER"/>
                    <spring:message code="notification.user"/>
                </label>
            </div>

            <div class="person--role">
                <label class="checkbox">
                    <form:checkbox path="permissions" value="BOSS"/><b><spring:message code="role.boss"/></b>:
                    <spring:message code="role.boss.dsc"/>
                </label>
                <label class="checkbox person--mail-notification">
                    <form:checkbox path="notifications" value="NOTIFICATION_BOSS"/>
                    <spring:message code="notification.boss"/>
                </label>
            </div>

            <div class="person--role">
                <label class="checkbox">
                    <form:checkbox path="permissions" value="OFFICE"/><b><spring:message code="role.office"/></b>:
                    <spring:message code="role.office.dsc"/>
                </label>
                <label class="checkbox person--mail-notification">
                    <form:checkbox path="notifications" value="NOTIFICATION_OFFICE"/>
                    <spring:message code="notification.office"/>
                </label>
            </div>

        </div>

    </div>

</div>

</div>


<div class="row">

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend>
            <p>
                <spring:message code="entitlement"/>
            </p>
        </legend>

    </div>

    <c:if test="${not empty errors}">
        <div class="col-md-11 alert alert-danger"><form:errors cssClass="error"/></div>
    </c:if>
    
    <div class="form-group">
        <label class="control-label col-md-4" for="year-dropdown"><spring:message code='year'/></label>

        <div class="col-md-7">

            <c:choose>
                <c:when test="${person.id == null}">
                    <form:select path="year" size="1" id="year-dropdown" class="form-control">
                        <form:option value="${currentYear - 1}"><c:out value="${currentYear - 1}"/></form:option>
                        <form:option value="${currentYear}" selected="selected"><c:out
                                value="${currentYear}"/></form:option>
                        <form:option value="${currentYear + 1}"><c:out value="${currentYear + 1}"/></form:option>
                        <form:option value="${currentYear + 2}"><c:out value="${currentYear + 2}"/></form:option>
                    </form:select>
                </c:when>
                <c:otherwise>
                    <form:select path="year" size="1" onchange="change(this.options[this.selectedIndex].value);"
                                 id="year-dropdown" class="form-control">
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

    <div class="form-group">
        <label class="control-label col-md-4">
            <spring:message code='From'/>
        </label>

        <div class="col-md-2">
            <person:day-dropdown path="dayFrom" selected="${personForm.dayFrom}" />
        </div>

        <div class="col-md-5">
            <person:month-dropdown path="monthFrom" selected="${personForm.monthFrom}" />
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4">
            <spring:message code='To'/>
        </label>

        <div class="col-md-2">
            <person:day-dropdown path="dayTo" selected="${personForm.dayTo}" />
        </div>

        <div class="col-md-5">
            <person:month-dropdown path="monthTo" selected="${personForm.monthTo}" />
        </div>

    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="annualVacationDays"><spring:message code='person.annual.vacation'/></label>

        <div class="col-md-7">
            <form:input path="annualVacationDays" class="form-control" cssErrorClass="form-control error" size="1" id="annualVacationDays"/>
            <span class="help-inline"><form:errors path="annualVacationDays" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="remainingVacationDays">
            <spring:message code="remaining"/>&nbsp;<spring:message code="last.year"/>
        </label>

        <div class="col-md-7">
            <form:input path="remainingVacationDays" class="form-control" cssErrorClass="form-control error" size="1" id="remainingVacationDays"/>
            <span class="help-inline"><form:errors path="remainingVacationDays" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4">
            <spring:message code='person.expire'/>
        </label>

        <div class="col-md-7 radio">
            <label class="halves">
                <form:radiobutton id="expireYes" path="remainingVacationDaysExpire" value="true"/>
                <spring:message code='yes'/>
            </label>

            <label class="halves">
                <form:radiobutton id="expireNo" path="remainingVacationDaysExpire" value="false"/>
                <spring:message code='no'/>
            </label>
        </div>
    </div>

</div>

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend>
            <p>
                <spring:message code="working.times"/>
            </p>
        </legend>

    </div>

    <c:if test="${fn:length(workingTimes) > 1}">

        <div class="form-group">
            <label class="control-label col-md-4"><spring:message code='working.times'/></label>

            <div class="controls col-md-7">
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

    <div class="form-group">
        <label class="control-label col-md-4">
            <spring:message code="working.time.valid.from" />
        </label>
        <div class="col-md-7">
            <form:input id="validFrom" path="validFrom" class="form-control" cssErrorClass="form-control error" placeholder="dd.MM.yyyy" />
            <span class="help-inline"><form:errors path="validFrom" cssClass="error"/></span>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-md-4">
            <spring:message code="working.time.days" />
        </label>
        <div class="col-md-7 checkbox">
            <c:forEach items="${weekDays}" var="weekDay">
                <label class="checkbox" for="${weekDay.name}">
                    <form:checkbox id="${weekDay.name}" path="workingDays" value="${weekDay.dayOfWeek}" />
                    &nbsp;<spring:message code='${weekDay.name}'/>
                </label>
            </c:forEach>
        </div>
    </div>

</div>

</div>

<div class="row">
    <div class="col-xs-12">

        <hr/>

        <button class="btn btn-large btn-success col-xs-12 col-md-3" type="submit"><i class='fa fa-check'></i>&nbsp;<spring:message code="save" /></button>
        <a class="btn btn-default btn-large col-xs-12 col-md-3" href="${formUrlPrefix}/staff"><i class='fa fa-remove'></i>&nbsp;<spring:message code='cancel'/></a>

    </div>
</div>

</form:form>

</div>
</div>

</body>

</html>
