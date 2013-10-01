<%-- 
    Document   : person_form
    Created on : 31.10.2011, 10:00:10
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>

<head>
    <title><spring:message code="title"/></title>
    <%@include file="../include/header.jsp" %>
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
    <style type="text/css">
        .td-name {
            width: 40%;
        }
    </style>
</head>

<body>

<%@include file="../include/menu_header.jsp" %>

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
<c:if test="${not empty errors}">
    <div class="grid_6" id="error-div"><form:errors cssClass="error"/></div>
</c:if>

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
        <label class="control-label" for="vorname"><spring:message code='firstname'/></label>

        <div class="controls">
            <form:input id="vorname" path="firstName" cssErrorClass="error"/>
            <span class="help-inline"><form:errors path="firstName" cssClass="error"/></span>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="nachname"><spring:message code='lastname'/></label>

        <div class="controls">
            <form:input id="nachname" path="lastName" cssErrorClass="error"/>
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
        <label class="control-label"><spring:message code='time'/></label>

        <div class="controls">

            <form:select path="dayFrom" size="1">
                <script type="text/javascript">
                    var i = 1;
                    for (i = 1; i < 32; i++) {
                        if (<c:out value="${personForm.dayFrom}" /> == i
                    )
                        {
                            document.write('<form:option selected="selected" value="' + i + '">' + i + '</form:option>');
                        }
                    else
                        {
                            document.write('<form:option value="' + i + '">' + i + '</form:option>');
                        }
                    }
                </script>
            </form:select>
            <form:select path="monthFrom" size="1">
                <script type="text/javascript">
                    var regional = "${pageContext.request.locale.language}";
                    var monthNames;

                    if (regional == "en") {
                        monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
                            'July', 'August', 'September', 'October', 'November', 'December'];
                    } else {
                        // default = german
                        monthNames = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
                            'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'];
                    }

                    var i = 1;
                    for (i = 1; i < 13; i++) {
                        if (<c:out value="${personForm.monthFrom}" /> == i
                    )
                        {
                            document.write('<form:option selected="selected" value="' + i + '">' + monthNames[i - 1] + '</form:option>');
                        }
                    else
                        {
                            document.write('<form:option value="' + i + '">' + monthNames[i - 1] + '</form:option>');
                        }
                    }
                </script>
            </form:select>

            <spring:message code='to'/>
            <form:select path="dayTo" size="1">
                <script type="text/javascript">
                    var i = 1;
                    for (i = 1; i < 32; i++) {
                        if (<c:out value="${personForm.dayTo}" /> == i
                    )
                        {
                            document.write('<form:option selected="selected" value="' + i + '">' + i + '</form:option>');
                        }
                    else
                        {
                            document.write('<form:option value="' + i + '">' + i + '</form:option>');
                        }
                    }
                </script>
            </form:select>
            <form:select path="monthTo" size="1">
                <script type="text/javascript">
                    var regional = "${pageContext.request.locale.language}";
                    var monthNames;

                    if (regional == "en") {
                        monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
                            'July', 'August', 'September', 'October', 'November', 'December'];
                    } else {
                        // default = german
                        monthNames = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
                            'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'];
                    }

                    var i = 1;
                    for (i = 1; i < 13; i++) {
                        if (<c:out value="${personForm.monthTo}" /> == i
                    )
                        {
                            document.write('<form:option selected="selected" value="' + i + '">' + monthNames[i - 1] + '</form:option>');
                        }
                    else
                        {
                            document.write('<form:option value="' + i + '">' + monthNames[i - 1] + '</form:option>');
                        }
                    }
                </script>
            </form:select>

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
