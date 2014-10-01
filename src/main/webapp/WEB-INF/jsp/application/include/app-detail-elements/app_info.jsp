<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="box">
    <span class="thirds">
       <span class="box-icon bg-yellow"><i class="fa fa-sun-o"></i></span>
       <span class="box-icon state ${application.status} pull-right hidden-print" title="<spring:message code='${application.status.state}' />">
            <c:choose>
                <c:when test="${application.status == 'WAITING'}">
                    <i class="fa fa-question"></i>
                </c:when>
                <c:when test="${application.status == 'ALLOWED'}">
                    <i class="fa fa-check"></i>
                </c:when>
                <c:when test="${application.status == 'REJECTED'}">
                    <i class="fa fa-ban"></i>
                </c:when>
                <c:when test="${application.status == 'CANCELLED'}">
                    <i class="fa fa-trash"></i>
                </c:when>
                <c:otherwise>
                    &nbsp;
                </c:otherwise>
            </c:choose>
        </span>

       <spring:message code="app.apply"/> <h4><spring:message code="${application.vacationType.vacationTypeName}"/></h4>

        <c:choose>
            <c:when test="${application.startDate == application.endDate}">
                <spring:message code="at"/> <h4 class="is-inline-block is-sticky"><uv:date date="${application.startDate}"/>,
                <spring:message code="${application.howLong.dayLength}"/></h4>
            </c:when>
            <c:otherwise>
                <spring:message code="from"/> <h4 class="is-inline-block is-sticky"><uv:date
                    date="${application.startDate}"/></h4> <spring:message code="to"/> <h4 class="is-inline-block is-sticky">
                <uv:date date="${application.endDate}"/></h4>
            </c:otherwise>
        </c:choose>

    </span>
</div>

<table class="list-table striped-table bordered-table" cellspacing="0">

    <tr>
        <td><spring:message code="app.time" /></td>
        <td>
            <b class="days">
                <%-- filled by javascript --%>
            </b>
            <script type="text/javascript">

                $(document).ready(function () {

                    var dayLength = '<c:out value="${application.howLong}" />';
                    var personId = '<c:out value="${application.person.id}" />';

                    var startDate = "<joda:format pattern='yyyy/MM/dd' value='${application.startDate}' />";
                    var endDate = "<joda:format pattern='yyyy/MM/dd' value='${application.endDate}' />";

                    var from = new Date(startDate);
                    var to = new Date(endDate);

                    sendGetDaysRequest("<spring:url value='/api' />", from, to, dayLength, personId, ".days", true);

                });

            </script>
        </td>
    </tr>
    <tr class="visible-print">
        <td><spring:message code="staff" /></td>
        <td><b><c:out value="${application.person.niceName}" /></b></td>
    </tr>
    <tr class="visible-print">
        <td><spring:message code="state" /></td>
        <td><spring:message code="${application.status.state}" /></td>
    </tr>
    <tr>
        <td>
            <spring:message code='reason'/>
        </td>
        <td>
            <c:choose>
                <c:when test="${application.reason != null && !empty application.reason}">
                    <c:out value="${application.reason}"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="not.stated"/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>

    <tr>
        <td>
            <spring:message code='app.rep'/>
        </td>
        <td>
            <c:choose>
                <c:when test="${application.repDeprecated != null}">
                    <c:out value="${application.repDeprecated}"/>
                </c:when>
                <c:otherwise>
                    <c:choose>
                        <c:when test="${application.rep != null}">
                            <c:out value="${application.rep.niceName}"/>
                        </c:when>
                        <c:otherwise>
                            <spring:message code="not.stated"/>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <td>
            <spring:message code='app.address'/>
        </td>
        <td>
            <c:choose>
                <c:when test="${application.address!= null && !empty application.address}">
                    <c:out value="${application.address}"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="not.stated"/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <td>
            <spring:message code='app.team'/>
        </td>
        <td>
            <c:choose>
                <c:when test="${application.teamInformed == true}">
                    <i class="fa fa-check hidden-print"></i>
                    <spring:message code='yes'/>
                </c:when>
                <c:otherwise>
                    <i class="fa fa-remove hidden-print"></i>
                    <spring:message code='no'/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
</table>
