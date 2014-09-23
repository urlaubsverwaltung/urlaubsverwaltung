<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<div class="grid-100">

    <div class="header">

        <legend>
            <p>
                <spring:message code="apps.vac" />
            </p>
            
                <c:choose>
                    <c:when test="${person.id == loggedUser.id}">
                        <a class="btn btn-default pull-right" href="${formUrlPrefix}/application/new">
                            <i class="icon-pencil"></i>&nbsp;<spring:message code="ov.apply"/>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <sec:authorize access="hasRole('OFFICE')">
                            <c:if test="${person.id != loggedUser.id}">
                                <a class="btn btn-default"
                                   href="${formUrlPrefix}/${person.id}/application/new">
                                    <c:set var="staff" value="${person.firstName} ${person.lastName}"/>
                                    <i class="icon-pencil"></i>&nbsp;<spring:message code="ov.apply"/>
                                </a>
                            </c:if>
                        </sec:authorize>
                    </c:otherwise>
                </c:choose>

        </legend>

    </div>

        <c:choose>

            <c:when test="${empty applications}">
                <p>
                    <spring:message code='no.apps' />
                </p>
            </c:when>

            <c:otherwise>

                <%-- has css class tablesorter only because of styling, is not sortable --%>
                <table class="data-table is-centered tablesorter overview-tbl zebra-table" cellspacing="0">
                    <tr>
                        <th>
                            <spring:message code="state" />
                        </th>
                        <th>
                            <spring:message code="type" />
                        </th>
                        <th>
                            <spring:message code="time" />
                        </th>
                        <th>
                            <spring:message code="days.vac" />
                        </th>
                    </tr>

                    <c:forEach items="${applications}" var="app" varStatus="loopStatus">
                        <c:choose>
                            <c:when test="${app.status.state == 'state.cancelled' || app.status.state == 'state.rejected'}">
                                <c:set var="CSS_CLASS" value="inactive" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="CSS_CLASS" value="active" />
                            </c:otherwise>
                        </c:choose>
                        <tr class="${CSS_CLASS}" onclick="navigate('${formUrlPrefix}/application/${app.id}');">
                            <td>
                            <span class="print--visible">
                                <spring:message code="${app.status.state}" />
                            </span>
                            <span class="print--invisible">
                                 <c:choose>
                                     <c:when test="${app.status.state == 'state.waiting'}">
                                         <b style="font-size: 15px">?</b>
                                     </c:when>
                                     <c:when test="${app.status.state == 'state.allowed'}">
                                         <i class="icon-ok"></i>
                                     </c:when>
                                     <c:when test="${app.status.state == 'state.rejected'}">
                                         <i class="icon-ban-circle"></i>
                                     </c:when>
                                     <c:when test="${app.status.state == 'state.cancelled'}">
                                         <i class="icon-trash"></i>
                                     </c:when>
                                     <c:otherwise>
                                         &nbsp;
                                     </c:otherwise>
                                 </c:choose>
                            </span>
                            </td>
                            <td class="${app.vacationType}">
                                <spring:message code="${app.vacationType.vacationTypeName}"/>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${app.startDate == app.endDate}">
                                        <uv:date date="${app.startDate}" />, <spring:message code="${app.howLong.dayLength}" />
                                    </c:when>
                                    <c:otherwise>
                                        <uv:date date="${app.startDate}" /> - <uv:date date="${app.endDate}" />
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="days-${loopStatus.index}">

                                <script type="text/javascript">

                                    $(document).ready(function() {

                                        var dayLength = '<c:out value="${app.howLong}" />';
                                        var personId = '<c:out value="${app.person.id}" />';

                                        var startDate= "<joda:format pattern='yyyy/MM/dd' value='${app.startDate}' />";
                                        var endDate = "<joda:format pattern='yyyy/MM/dd' value='${app.endDate}' />";

                                        var from = new Date(startDate);
                                        var to = new Date(endDate);

                                        sendGetDaysRequest("<spring:url value='/api' />", from, to, dayLength, personId, ".days-${loopStatus.index}");

                                    });

                                </script>
                            </td>
                        </tr>
                    </c:forEach>
                </table>

            </c:otherwise>

        </c:choose>
    </div>
