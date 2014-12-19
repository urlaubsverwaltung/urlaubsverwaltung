<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head/>

    <%@include file="./include/app-form-elements/datepicker.jsp" %>
    <%@include file="./include/app-form-elements/day-length-selector.jsp" %>

    <script type="text/javascript">
        $(function() {

            <%-- SPECIAL LEAVE INFO --%>
            $(document).ready(function () {
                $('#special-leave-info').popover();
            });


            <%-- CALENDAR: PRESET DATE IN APP FORM ON CLICKING DAY --%>

            function preset(id, dateString) {

                var match = dateString.match(/\d+/g);

                var y = match[0];
                var m = match[1] - 1;
                var d = match[2];

                $(id).datepicker('setDate', new Date(y, m , d));
            }

            var from = '${param.from}';
            var to   = '${param.to}';

            if (from) {
                preset('#from', from);
                preset('#to'  , to || from);

                sendGetDaysRequest("<spring:url value='/api' />",
                        $("#from").datepicker("getDate"),
                        $("#to").datepicker("getDate"),
                        $('input:radio[name=howLong]:checked').val(),
                        '<c:out value="${person.id}" />', ".days", true);
            }
        });
    </script>

</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="content">

<div class="container">

<c:choose>

<c:when test="${notpossible == true}">

    <spring:message code="app.not.possible"/>

</c:when>

<c:otherwise>

<c:choose>
    <c:when test="${person.id == loggedUser.id}">
        <c:set var="appliesAsRep" value="false"/>
        <c:set var="actionUrl" value="${URL_PREFIX}/application/new"/>
    </c:when>
    <c:otherwise>
        <sec:authorize access="hasRole('OFFICE')">
            <c:set var="appliesAsRep" value="true"/>
            <c:set var="actionUrl" value="${URL_PREFIX}/application/new?personId=${person.id}"/>
        </sec:authorize>
    </c:otherwise>
</c:choose>

<form:form method="POST" action="${actionUrl}" modelAttribute="appForm" class="form-horizontal" role="form">
<form:hidden path="person" value="${person.id}" />

<c:if test="${not empty errors || timeError != null}">

    <div class="row">

    <div class="col-xs-12 alert alert-danger">
            <c:if test="${empty errors}">
                <spring:message code="${timeError}"/>
            </c:if>
            <form:errors />
            <c:if test="${daysApp != null}">
                <c:choose>
                    <c:when test="${daysApp <= 1.00 && daysApp > 0.50}">
                        <c:set var="msg1" value="error.days.start.sing"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="msg1" value="error.days.start.plural"/>
                    </c:otherwise>
                </c:choose>
                <c:set var="numberOfDays" value="${leftDays}"/>
                <c:choose>
                    <c:when test="${numberOfDays <= 1.00 && numberOfDays > 0.50}">
                        <c:set var="msg2" value="error.days.end.sing"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="msg2" value="error.days.end.plural"/>
                    </c:otherwise>
                </c:choose>
                <spring:message code="${msg2}" arguments="${numberOfDays}"/>
            </c:if>
    </div>

    </div>
</c:if>

<div class="row">

<div class="col-xs-12 col-sm-12 col-md-6">

<div class="header">

    <legend>
        <p>
            <spring:message code="app.title" />
        </p>
    </legend>

</div>

    <c:if test="${appliesAsRep == true}">
        <%-- office applies for a user --%>

        <div class="form-group">
            <label class="control-label col-md-4">
                <spring:message code="name"/>
            </label>
            <div class="col-md-7">
                <select id="person-select" class="form-control" onchange="window.location.href=this.options
                                                        [this.selectedIndex].value">
                    <option value="${URL_PREFIX}/${person.id}/application/new" selected="selected">
                        <c:out value="${person.niceName}"/>
                    </option>
                    <c:forEach items="${personList}" var="p">
                        <c:if test="${person.id != p.id}">
                            <option value="${URL_PREFIX}/${p.id}/application/new">
                                <c:out value="${p.niceName}"/>
                            </option>
                        </c:if>
                    </c:forEach>
                </select>
            </div>
        </div>

    </c:if>

<div class="form-group">

    <i class="fa fa-question-circle fa-action hidden-sm hidden-xs special-leave--info-modal" data-toggle="modal" data-target="#special-leave-modal"></i>

    <label class="control-label col-md-4" for="vacationType">
        <spring:message code='app.type' />
    </label>

    <div class="col-md-7">
        <form:select path="vacationType" size="1" id="vacationType" class="form-control" onchange="checkSonderurlaub(value);">
            <c:choose>
                <c:when test="${appForm.vacationType == null}">
                    <c:forEach items="${vacTypes}" var="vacType">
                        <option value="${vacType}">
                            <spring:message code='${vacType}'/>
                        </option>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <option value="${appForm.vacationType}" selected="selected">
                        <spring:message code='${appForm.vacationType}'/>
                    </option>
                    <c:forEach items="${vacTypes}" var="vacType">
                        <c:if test="${vacType != appForm.vacationType}">
                            <option value="${vacType}">
                                <spring:message code='${vacType}'/>
                            </option>
                        </c:if>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </form:select>
    </div>

    <!-- Modal -->
    <div id="special-leave-modal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
         aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                    <h4 id="myModalLabel" class="modal-title"><spring:message code='special.leave.title.long'/></h4>
                </div>
                <div class="modal-body">
                    <p><spring:message code='special.leave.explanation'/></p>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-default" data-dismiss="modal" aria-hidden="true"><i class="fa fa-remove"></i> <spring:message code='close'/></button>
                </div>
            </div>
        </div>
    </div>

</div>

<div class="form-group">
    <label class="control-label col-md-4">
        <spring:message code="time"/>
    </label>

    <div class="col-md-7 radio">

        <label class="thirds">
            <form:radiobutton id="fullDay" class="dayLength-full" path="howLong" checked="checked" value="FULL" />
            <spring:message code='FULL'/>
        </label>

        <label class="thirds">
            <form:radiobutton id="morning" class="dayLength-half" path="howLong" value="MORNING" />
            <spring:message code='MORNING'/>
        </label>

        <label class="thirds">
            <form:radiobutton id="noon" class="dayLength-half" path="howLong" value="NOON" />
            <spring:message code='NOON'/>
        </label>

    </div>

</div>

    <div class="form-group full-day">
    <label class="col-md-4 control-label" for="from">
        <spring:message code="From" />:
    </label>
    <div class="col-md-7">
        <form:input id="from" path="startDate" class="form-control" cssErrorClass="form-control error" />
    </div>
</div>

<div class="form-group full-day">
    <label class="control-label col-md-4" for="to">
        <spring:message code="To" />:
    </label>
    <div class="col-md-7">
        <form:input id="to" path="endDate" class="form-control" cssErrorClass="form-control error" />
        <span class="help-block info days"></span>
    </div>
</div>

<div class="form-group half-day">
    <label class="control-label col-md-4" for="at">
        <spring:message code="At" />:
    </label>
    <div class="col-md-7">
        <form:input id="at" path="startDateHalf" class="form-control" cssErrorClass="form-control error" />
        <span class="help-block info days"></span>
    </div>
</div>

<div class="form-group">
    <label class="control-label col-md-4" for="rep">
        <spring:message code="app.rep"/>
    </label>

    <div class="col-md-7">
        <form:select path="rep" id="rep" size="1" class="form-control">
            <option value="-1"><spring:message code='app.no.rep'/></option>
            <c:forEach items="${persons}" var="person">

               <c:choose>
                   <c:when test="${appForm.rep.id == person.id}">
                       <form:option value="${person.id}" selected="selected">
                           <c:out value="${person.niceName}" />
                       </form:option>
                   </c:when>
                   <c:otherwise>
                       <form:option value="${person.id}">
                           <c:out value="${person.niceName}" />
                       </form:option>
                   </c:otherwise>
               </c:choose>
            </c:forEach>
        </form:select>
        <form:errors path="rep" cssClass="error"/>
    </div>

</div>

<div class="form-group">
    <label class="control-label col-md-4">
        <spring:message code='app.team'/>
    </label>

    <div class="col-md-7 radio">

        <label class="halves">
            <form:radiobutton id="teamInformed" path="teamInformed" value="true"/>
            <spring:message code='yes'/>
        </label>

        <label class="halves">
            <form:radiobutton id="teamNotInformed" path="teamInformed" value="false"/>
            <spring:message code='no'/>
        </label>

        <form:errors path="teamInformed" cssClass="error"/>
    </div>

</div>

</div>

<div class="col-xs-12 col-md-6">

    <div class="header">

        <legend>
            <p>
                <spring:message code="app.further.info"/>
            </p>
        </legend>

    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="reason">
            <spring:message code="reason"/>
        </label>

        <div class="col-md-7">
            <span id="text-reason"></span><spring:message code='max.chars'/>
            <form:textarea id="reason" rows="1" path="reason" class="form-control" cssErrorClass="form-control error"
                           onkeyup="count(this.value, 'text-reason');"
                           onkeydown="maxChars(this,200); count(this.value, 'text-reason');"/>
            <form:errors path="reason" cssClass="error"/>
        </div>

    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="address">
            <spring:message code='app.address'/>:
        </label>

        <div class="col-md-7">
            <span id="text-address"></span><spring:message code="max.chars"/>
            <form:textarea id="address" rows="1" path="address" class="form-control" cssErrorClass="form-control error"
                           onkeyup="count(this.value, 'text-address');"
                           onkeydown="maxChars(this,200); count(this.value, 'text-address');"/>
            <form:errors path="address" cssClass="error"/>
        </div>

    </div>

    <div class="form-group">
        <label class="control-label col-md-4" for="comment">
            <spring:message code='app.form.comment'/>:
        </label>

        <div class="col-md-7">
            <span id="text-comment"></span><spring:message code="max.chars"/>
            <form:textarea id="comment" rows="1" path="comment" class="form-control" cssErrorClass="form-control error"
                           onkeyup="count(this.value, 'text-comment');"
                           onkeydown="maxChars(this,200); count(this.value, 'text-comment');"/>
            <form:errors path="comment" cssClass="error"/>
        </div>

    </div>

</div>

</div>

<div class="row">

    <div class="col-xs-12">

        <hr/>

        <button type="submit" class="btn btn-large btn-success pull-left col-xs-12 col-md-3">
            <i class='fa fa-check'></i>&nbsp;<spring:message code='apply'/>
        </button>

    </div>

</div>

</div>

</form:form>

</c:otherwise>
</c:choose>


</div>
<!-- End of grid container -->

</div>

</body>

</html>
