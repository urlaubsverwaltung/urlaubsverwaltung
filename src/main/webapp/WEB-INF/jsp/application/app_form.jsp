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

</head>

<body>

<spring:url var="formUrlPrefix" value="/web"/>

<uv:menu/>

<div class="content">

<div class="grid-container">

<c:choose>

<c:when test="${notpossible == true}">

    <spring:message code="app.not.possible"/>

</c:when>

<c:otherwise>

<c:choose>
    <c:when test="${setForce != null}">
        <c:set var="forcy" value="${setForce}"/>
    </c:when>
    <c:otherwise>
        <c:set var="forcy" value="0"/>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${person.id == loggedUser.id}">
        <c:set var="appliesAsRep" value="false"/>
        <c:set var="actionUrl" value="${formUrlPrefix}/application/new?force=${forcy}"/>
    </c:when>
    <c:otherwise>
        <sec:authorize access="hasRole('OFFICE')">
            <c:set var="appliesAsRep" value="true"/>
            <c:set var="actionUrl" value="${formUrlPrefix}/${person.id}/application/new?force=${forcy}"/>
        </sec:authorize>
    </c:otherwise>
</c:choose>

<form:form method="POST" action="${actionUrl}" modelAttribute="appForm" class="form-horizontal" role="form">

<c:if test="${not empty errors || timeError != null}">

    <div class="grid-100 alert alert-danger">
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
</c:if>

<div class="grid-50">

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
            <label class="control-label col-sm-4">
                <spring:message code="name"/>
            </label>
            <div class="col-sm-9">
                <select id="person-select" class="form-control" onchange="window.location.href=this.options
                                                        [this.selectedIndex].value">
                    <option value="${formUrlPrefix}/${person.id}/application/new" selected="selected">
                        <c:out value="${person.niceName}"/>
                    </option>
                    <c:forEach items="${personList}" var="p">
                        <c:if test="${person.id != p.id}">
                            <option value="${formUrlPrefix}/${p.id}/application/new">
                                <c:out value="${p.niceName}"/>
                            </option>
                        </c:if>
                    </c:forEach>
                </select>
            </div>
        </div>

    </c:if>

<div class="form-group">
    <i class="fa fa-question-circle fa-action" id="special-leave-info" style="vertical-align: middle; line-height: 32px"
       data-placement="bottom" data-toggle="popover" data-html="true"
       data-original-title="<spring:message code='special.leave.title.short' />"
       data-content="<spring:message code='special.leave.explanation' />" data-trigger="hover"></i>

    <label class="control-label col-sm-4" for="vacationType">
        <spring:message code='app.type' />
    </label>

    <div class="col-sm-7">
        <form:select path="vacationType" size="1" id="vacationType" class="form-control" onchange="checkSonderurlaub(value);">
            <c:choose>
                <c:when test="${appForm.vacationType == null}">
                    <c:forEach items="${vacTypes}" var="vacType">
                        <option value="${vacType}">
                            <spring:message code='${vacType.vacationTypeName}'/>
                        </option>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <option value="${appForm.vacationType}" selected="selected">
                        <spring:message code='${appForm.vacationType.vacationTypeName}'/>
                    </option>
                    <c:forEach items="${vacTypes}" var="vacType">
                        <c:if test="${vacType != appForm.vacationType}">
                            <option value="${vacType}">
                                <spring:message code='${vacType.vacationTypeName}'/>
                            </option>
                        </c:if>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </form:select>
    </div>

    <script type="text/javascript">

        $(document).ready(function () {
            $('#special-leave-info').popover();
        });

    </script>

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
                    <button class="btn btn-default" data-dismiss="modal" aria-hidden="true"><spring:message code='close'/></button>
                </div>
            </div>
        </div>
    </div>

</div>

<div class="form-group">
    <label class="control-label col-sm-4">
        <spring:message code="time"/>
    </label>

    <div class="col-sm-7 radio">

        <label>
            <form:radiobutton id="fullDay" class="dayLength-full" path="howLong" checked="checked" value="${full}" />
            <spring:message code='${full.dayLength}'/>
        </label>

        <label>
            <form:radiobutton id="morning" class="dayLength-half" path="howLong" value="${morning}" />
            <spring:message code='${morning.dayLength}'/>
        </label>

        <label>
            <form:radiobutton id="noon" class="dayLength-half" path="howLong" value="${noon}" />
            <spring:message code='${noon.dayLength}'/>
        </label>

    </div>

</div>

    <div class="form-group full-day">
    <label class="col-sm-4 control-label" for="from">
        <spring:message code="From" />:
    </label>
    <div class="col-sm-7">
        <form:input id="from" path="startDate" class="form-control" cssErrorClass="form-control error" />
    </div>
</div>

<div class="form-group full-day">
    <label class="control-label col-sm-4" for="to">
        <spring:message code="To" />:
    </label>
    <div class="col-sm-7">
        <form:input id="to" path="endDate" class="form-control" cssErrorClass="form-control error" />
        <span class="help-block info days"></span>
    </div>
</div>

<div class="form-group half-day">
    <label class="control-label col-sm-4" for="at">
        <spring:message code="At" />:
    </label>
    <div class="col-sm-7">
        <form:input id="at" path="startDateHalf" class="form-control" cssErrorClass="form-control error" />
        <span class="help-block info days"></span>
    </div>
</div>

<div class="form-group">
    <label class="control-label col-sm-4" for="rep">
        <spring:message code="app.rep"/>
    </label>

    <div class="col-sm-7">
        <form:select path="rep" id="rep" size="1" class="form-control">
            <c:choose>
                <c:when test="${appForm.rep == null}">
                    <option value="-1"><spring:message code='app.no.rep'/></option>
                    <c:forEach items="${persons}" var="staff">
                        <option value="${staff.id}">
                            <c:out value="${staff.niceName}"/>
                        </option>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <option value="${appForm.rep}" selected="selected">
                        <c:out value="${appForm.rep.niceName}"/>
                    </option>
                    <option value="-1"><spring:message code='app.no.rep'/></option>
                    <c:forEach items="${persons}" var="staff">
                        <option value="${staff.id}">
                            <c:out value="${staff.niceName}"/>
                        </option>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </form:select>
        <form:errors path="rep" cssClass="error"/>
    </div>

</div>

<div class="form-group">
    <label class="control-label col-sm-4">
        <spring:message code='app.team'/>
    </label>

    <div class="col-sm-7 radio">

        <label>
            <form:radiobutton id="teamInformed" path="teamInformed" value="true"/>
            <spring:message code='yes'/>
        </label>

        <label>
            <form:radiobutton id="teamNotInformed" path="teamInformed" value="false"/>
            <spring:message code='no'/>
        </label>

        <form:errors path="teamInformed" cssClass="error"/>
    </div>

</div>

</div>

<div class="grid-50">

    <div class="header">

        <legend>
            <p>
                <spring:message code="app.further.info"/>
            </p>
        </legend>

    </div>

    <div class="form-group">
        <label class="control-label col-sm-4" for="reason">
            <spring:message code="reason"/>
        </label>

        <div class="col-sm-7">
            <span id="text-reason"></span><spring:message code='max.chars'/>
            <form:textarea id="reason" rows="1" path="reason" class="form-control" cssErrorClass="form-control error"
                           onkeyup="count(this.value, 'text-reason');"
                           onkeydown="maxChars(this,200); count(this.value, 'text-reason');"/>
            <form:errors path="reason" cssClass="error"/>
        </div>

    </div>

    <div class="form-group">
        <label class="control-label col-sm-4" for="address">
            <spring:message code='app.address'/>:
        </label>

        <div class="col-sm-7">
            <span id="text-address"></span><spring:message code="max.chars"/>
            <form:textarea id="address" rows="1" path="address" class="form-control" cssErrorClass="form-control error"
                           onkeyup="count(this.value, 'text-address');"
                           onkeydown="maxChars(this,200); count(this.value, 'text-address');"/>
            <form:errors path="address" cssClass="error"/>
        </div>

    </div>

    <div class="form-group">
        <label class="control-label col-sm-4" for="comment">
            <spring:message code='app.form.comment'/>:
        </label>

        <div class="col-sm-7">
            <span id="text-comment"></span><spring:message code="max.chars"/>
            <form:textarea id="comment" rows="1" path="comment" class="form-control" cssErrorClass="form-control error"
                           onkeyup="count(this.value, 'text-comment');"
                           onkeydown="maxChars(this,200); count(this.value, 'text-comment');"/>
            <form:errors path="comment" cssClass="error"/>
        </div>

    </div>

</div>

<div class="grid-100">

    <hr/>

    <button type="submit" class="btn btn-large btn-success pull-left">
        <i class='fa fa-check'></i>&nbsp;<spring:message code='apply'/>
    </button>

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
