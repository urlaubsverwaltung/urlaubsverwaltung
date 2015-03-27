
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head />
</head>

<body>

<spring:url var="URL_PREFIX" value="/web" />

<uv:menu />

<div class="content">
    <div class="container">

        <div class="row">

            <div class="col-xs-12">

            <div class="header">

                <legend>
                    <p>
                      <spring:message code="sicknotes.statistics" />
                    </p>
                    <uv:year-selector year="${statistics.year}" />
                    <uv:print />
                </legend>

            </div>

            </div>

        </div>

        <div class="row">

            <div class="col-xs-12">
            
            <table class="list-table">

                <thead>
                    <tr>
                        <th colspan="2" class="text-right"><spring:message code="Effective"/> <uv:date date="${statistics.created}" /></th>
                    </tr>
                </thead>

                <tbody>
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
                        <uv:number number="${statistics.totalNumberOfSickDays}" />
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
                        <uv:number number="${statistics.averageDurationOfDiseasePerPerson}" />
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
