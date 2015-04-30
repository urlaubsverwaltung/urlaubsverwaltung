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
</head>

<body>

<uv:menu />

<spring:url var="URL_PREFIX" value="/web"/>

<div class="content">
    <div class="container">
    
        <div class="row">
            <div class="col-xs-12 col-sm-12 col-md-12 feedback">
                <c:if test="${success}">
                    <div class="alert alert-success">
                        <spring:message code="settings.action.success" />
                    </div>
                </c:if>
            </div>
            <div class="col-xs-12 header">
            
                <legend>
                    <p>
                        <spring:message code="settings.title" />
                    </p>
                </legend>
            
            </div>
        
            <form:form method="PUT" action="${URL_PREFIX}/settings" modelAttribute="settings" class="form-horizontal" role="form">
            <form:hidden path="id" />
            <div class="col-xs-12 col-md-6">                
                
                <div class="form-group">
                    <label class="control-label col-md-4" for="maximumAnnualVacationDays"><spring:message code='settings.maximumAnnualVacationDays'/></label>
                
                    <div class="col-md-8">
                        <form:input id="maximumAnnualVacationDays" path="maximumAnnualVacationDays" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="maximumAnnualVacationDays" cssClass="error"/></span>
                    </div>
                </div>
                
                <div class="form-group">
                    <label class="control-label col-md-4" for="maximumMonthsToApplyForLeaveInAdvance"><spring:message code='settings.maximumMonthsToApplyForLeaveInAdvance'/></label>
                
                    <div class="col-md-8">
                        <form:input id="maximumMonthsToApplyForLeaveInAdvance" path="maximumMonthsToApplyForLeaveInAdvance" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="maximumMonthsToApplyForLeaveInAdvance" cssClass="error"/></span>
                    </div>
                </div>
                
                <div class="form-group">
                    <label class="control-label col-md-4" for="maximumSickPayDays"><spring:message code='settings.maximumSickPayDays'/></label>
                
                    <div class="col-md-8">
                        <form:input id="maximumSickPayDays" path="maximumSickPayDays" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="maximumSickPayDays" cssClass="error"/></span>
                    </div>
                </div>
                
                <div class="form-group">
                    <label class="control-label col-md-4" for="daysBeforeEndOfSickPayNotification"><spring:message code='settings.daysBeforeEndOfSickPayNotification'/></label>
                
                    <div class="col-md-8">
                        <form:input id="daysBeforeEndOfSickPayNotification" path="daysBeforeEndOfSickPayNotification" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="daysBeforeEndOfSickPayNotification" cssClass="error"/></span>
                    </div>
                </div>
                
                </div>
            <div class="col-xs-12 col-md-6">
                                
                <div class="form-group">
                    <label class="control-label col-md-4" for="workingDurationForChristmasEve"><spring:message code='settings.workingDurationForChristmasEve'/></label>
                
                    <div class="col-md-8">
                       <form:select path="workingDurationForChristmasEve" id="dayLengthTypes" class="form-control" cssErrorClass="form-control error">
                           <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                               <form:option value="${dayLengthType}"><spring:message code="${dayLengthType}" /></form:option>
                           </c:forEach>
                       </form:select>
                    </div>
                </div>
                
                <div class="form-group">
                    <label class="control-label col-md-4" for="workingDurationForNewYearsEve"><spring:message code='settings.workingDurationForNewYearsEve'/></label>
                
                    <div class="col-md-8">
                        <form:select path="workingDurationForNewYearsEve" id="dayLengthTypes" class="form-control" cssErrorClass="form-control error">
                            <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                                <form:option value="${dayLengthType}"><spring:message code="${dayLengthType}" /></form:option>
                            </c:forEach>
                        </form:select>
                    </div>
                </div>
                
                <div class="form-group">
                    <label class="control-label col-md-4" for="federalStateType"><spring:message code='settings.federalState'/></label>
                
                    <div class="col-md-8">
                        <form:select path="federalState" id="federalStateType" class="form-control" cssErrorClass="form-control error">
                            <c:forEach items="${federalStateTypes}" var="federalStateType">
                                <form:option value="${federalStateType}"><spring:message code="federalState.${federalStateType}" /></form:option>
                            </c:forEach>
                        </form:select>
                    </div>
                </div>
            
        </div>        

      
        <div class="col-xs-12">
        
            <hr/>
        
            <button type="submit" class="btn btn-success pull-left col-xs-12 col-sm-5 col-md-2">
                <i class='fa fa-check'></i>&nbsp;<spring:message code='action.save'/>
            </button>
           
        </div>
        
        
        </form:form>
        
    </div>
</div>

</body>

</html>
