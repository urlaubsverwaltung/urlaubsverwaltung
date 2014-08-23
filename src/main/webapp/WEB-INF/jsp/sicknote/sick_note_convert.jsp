<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<html>
<head>
    <uv:head />

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

<uv:menu />

<div class="content">

    <div class="grid-container">

        <div class="grid-100">

            <div class="grid-50">
                <div class="overview-header">
                    <legend>
                        <p>
                            <spring:message code="sicknotes.convert.vacation" />
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
                        <label class="control-label">
                            <spring:message code="vac.type" />
                            <br />
                            <span class="help-inline"><form:errors path="vacationType" cssClass="error"/></span>
                        </label>

                        <div class="controls">
                            <form:select path="vacationType" size="1" cssClass="input-medium" cssErrorClass="input-medium error">
                                <c:forEach items="${vacTypes}" var="vacType">
                                    <option value="${vacType}">
                                        <spring:message code='${vacType.vacationTypeName}' />
                                    </option>
                                </c:forEach>
                            </form:select>
                        </div>
                    </div>
    
                    <div class="control-group">
                        <label class="control-label"><spring:message code="time" /></label>
    
                        <div class="controls">
                            <uv:date date="${sickNote.startDate}" /> - <uv:date date="${sickNote.endDate}" />
                        </div>
                    </div>
    
                    <div class="control-group">
                        <label class="control-label">
                            <label class="control-label" for="reason"><spring:message code="reason" /></label>
                            <span class="help-inline"><form:errors path="reason" cssClass="error"/></span>
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

            <div class="grid-50">

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
                        <td>
                            <uv:date date="${sickNote.startDate}" /> - <uv:date date="${sickNote.endDate}" />
                        </td>
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
                        <td>
                            <uv:date date="${sickNote.aubStartDate}" /> - <uv:date date="${sickNote.aubEndDate}" />
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            
        </div>
        
    </div>
    
</div>    

</body>
</html>