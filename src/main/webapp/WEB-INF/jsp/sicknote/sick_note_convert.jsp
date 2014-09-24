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
</head>
<body>

<spring:url var="formUrlPrefix" value="/web"/>

<uv:menu />

<div class="content">

    <c:set var="METHOD" value="POST" />
    <c:set var="ACTION" value="${formUrlPrefix}/sicknote/${sickNote.id}/convert" />

    <form:form method="${METHOD}" action="${ACTION}" modelAttribute="appForm" class="form-horizontal">

    <div class="container-fluid">

        <div class="row">

            <div class="col-xs-12 col-sm-6">
                <div class="header">
                    <legend>
                        <p>
                            <spring:message code="sicknotes.convert.vacation" />
                        </p>
                    </legend>
                </div>
    
                    <div class="form-group">
                        <label class="control-label col-sm-4"><spring:message code='staff'/></label>
    
                        <div class="col-sm-7">
                            <c:out value="${sickNote.person.firstName}" />&nbsp;<c:out value="${sickNote.person.lastName}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-sm-4">
                            <spring:message code="vac.type" />
                            <br />
                            <span class="help-inline"><form:errors path="vacationType" cssClass="error"/></span>
                        </label>

                        <div class="col-sm-7">
                            <form:select path="vacationType" size="1" cssClass="form-control" cssErrorClass="form-control error">
                                <c:forEach items="${vacTypes}" var="vacType">
                                    <option value="${vacType}">
                                        <spring:message code='${vacType.vacationTypeName}' />
                                    </option>
                                </c:forEach>
                            </form:select>
                        </div>
                    </div>
    
                    <div class="form-group">
                        <label class="control-label col-sm-4"><spring:message code="time" /></label>
    
                        <div class="col-sm-7">
                            <uv:date date="${sickNote.startDate}" /> - <uv:date date="${sickNote.endDate}" />
                        </div>
                    </div>
    
                    <div class="form-group">
                        <label class="control-label col-sm-4">
                            <spring:message code="reason" />
                        </label>

                        <div class="col-sm-7">
                            <span id="count-chars"></span><spring:message code="max.chars" />
                            <br />
                            <form:textarea id="reason" path="reason" cssClass="form-control" cssErrorClass="form-control error" rows="1" onkeyup="count(this.value, 'count-chars');" onkeydown="maxChars(this,200); count(this.value, 'count-chars');" />
                        </div>

                        <span class="help-inline"><form:errors path="reason" cssClass="error"/></span>
                    </div>

            </div>

            <div class="col-xs-12 col-sm-6">

                <div class="header">
                    <legend>
                        <p><spring:message code="sicknote" /></p>
                    </legend>
                </div>

                <table class="detail-table">
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
                                    <i class="fa fa-check"></i>
                                </c:when>
                                <c:otherwise>
                                    <i class="fa fa-remove"></i>
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

        <div class="row">

            <div class="col-xs-12">

                <hr/>

                <button class="btn btn-large btn-success col-xs-12 col-sm-2" type="submit"><i class='fa fa-check'></i>&nbsp;<spring:message code="save" /></button>
                <a class="btn btn-default btn-large col-xs-12 col-sm-2" href="${formUrlPrefix}/sicknote/${sickNote.id}"><i class='fa fa-remove'></i>&nbsp;<spring:message code='cancel'/></a>


            </div>

        </div>
        
    </div>

</form:form>
</div>    

</body>
</html>