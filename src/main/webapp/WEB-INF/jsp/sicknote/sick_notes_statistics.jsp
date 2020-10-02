<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="sicknotes.statistics.header.title" arguments="${statistics.year}"/>
    </title>
    <uv:custom-head/>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="content">
    <div class="container">

        <uv:section-heading>
            <jsp:attribute name="actions">
                <uv:print/>
            </jsp:attribute>
            <jsp:body>
                <h1>
                    <spring:message code="sicknotes.statistics.title"/>
                </h1>
                <uv:year-selector year="${statistics.year}" hrefPrefix="${URL_PREFIX}/sicknote/statistics?year="/>
            </jsp:body>
        </uv:section-heading>

        <div class="row">

            <div class="col-xs-12">

                <table class="list-table tw-text-sm">

                    <thead>
                        <tr>
                            <th scope="col" colspan="2" class="text-right"><spring:message code="filter.validity"/> <uv:date
                                date="${statistics.created}"/></th>
                        </tr>
                    </thead>

                    <tbody>
                    <tr>
                        <td>
                            <spring:message code="sicknotes.statistics.totalNumber"/>
                        </td>
                        <td>
                            <c:out value="${statistics.totalNumberOfSickNotes}"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <spring:message code="sicknotes.statistics.totalNumberOfDays"/>
                        </td>
                        <td>
                            <uv:number number="${statistics.totalNumberOfSickDays}"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <spring:message code="sicknotes.statistics.personWithSickNotes"/>
                        </td>
                        <td>
                            <c:out value="${statistics.numberOfPersonsWithMinimumOneSickNote}"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <spring:message code="sicknotes.statistics.averageSickTime"/>
                        </td>
                        <td>
                            <uv:number number="${statistics.averageDurationOfDiseasePerPerson}"/>
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
