
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head />
</head>

<body>

<spring:url var="formUrlPrefix" value="/web" />

<uv:menu />

<spring:url var="formUrlPrefix" value="/web" />

<div class="content">
    <div class="container">

        <div class="row">

            <div class="col-xs-12">

            <div class="header">

                <legend style="margin-bottom: 0">
                    <p>
                        <spring:message code="sicknotes" />
                    </p>
                    <a class="btn btn-default pull-right" href="#" media="print" onclick="window.print(); return false;">
                        <i class="fa fa-print"></i>&nbsp;<spring:message code='Print' />
                    </a>
                    <a class="btn btn-default pull-right" href="${formUrlPrefix}/sicknote/">
                        <i class="fa fa-arrow-circle-left"></i>&nbsp;<spring:message code='back' />
                    </a>
                </legend>

            </div>

            </div>

        </div>

        <div class="row">

            <div class="col-xs-12">
                <p style="float:left">
                    <spring:message code="sicknotes.statistics" />&nbsp;${statistics.year}
                </p>
                <p style="float:right">
                    <spring:message code="Effective"/> <uv:date date="${statistics.created}" />
                </p>
            </div>

        </div>
        
        <div class="row">

            <div class="col-xs-12">
            
            <table class="table">
                <tr>
                    <td>
                        <spring:message code="sicknotes.number" />
                    </td>
                    <td>
                        <c:out value="${statistics.totalNumberOfSickNotes}" /> 
                    </td>
                </tr>
                <tr>
                    <td>
                        <spring:message code="sicknotes.days.number" />  
                    </td>
                    <td>
                        <fmt:formatNumber maxFractionDigits="1" value="${statistics.totalNumberOfSickDays}" />
                    </td>
                </tr>
                <tr>
                    <td>
                        <spring:message code="sicknotes.staff.number" />
                    </td>
                    <td>
                        <c:out value="${statistics.numberOfPersonsWithMinimumOneSickNote}" />
                    </td>
                </tr>
                <tr>
                    <td>
                        <spring:message code="sicknotes.staff.average" />
                    </td>
                    <td>
                        <fmt:formatNumber maxFractionDigits="1" value="${statistics.averageDurationOfDiseasePerPerson}" />
                    </td>
                </tr>
            </table>

            </div>
            
        </div>
    </div>
</div>

</body>

</html>
