<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<table class="list-table selectable-table tw-text-sm">
    <tbody>
    <c:forEach items="${applications}" var="app" varStatus="loopStatus">

        <c:choose>
            <c:when test="${app.status == 'CANCELLED' || app.status == 'REJECTED' || app.status == 'REVOKED'}">
                <c:set var="CSS_CLASS" value="inactive"/>
            </c:when>
            <c:otherwise>
                <c:set var="CSS_CLASS" value="active"/>
            </c:otherwise>
        </c:choose>

        <tr class="${CSS_CLASS}" onclick="navigate('${URL_PREFIX}/application/${app.id}');">
            <td class="visible-print">
                <span>
                    <spring:message code="${app.status}"/>
                </span>
            </td>
            <td class="is-centered state ${app.status} print:tw-hidden" title="<spring:message code='${app.status}' />">
                <c:choose>
                    <c:when test="${app.status == 'WAITING'}">
                        <icon:question-mark-circle className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:when test="${app.status == 'ALLOWED'}">
                        <icon:check-circle className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:when test="${app.status == 'TEMPORARY_ALLOWED'}">
                        <icon:check-circle className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:when test="${app.status == 'ALLOWED_CANCELLATION_REQUESTED'}">
                        <icon:check-circle className="tw-w-6 tw-h-6" />
                        <icon:arrow-narrow-right className="tw-w-5 tw-h-5" />
                        <icon:trash className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:when test="${app.status == 'REJECTED'}">
                        <icon:ban className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:when test="${app.status == 'CANCELLED'  || app.status == 'REVOKED'}">
                        <icon:trash className="tw-w-6 tw-h-6" />
                    </c:when>
                    <c:otherwise>
                        &nbsp;
                    </c:otherwise>
                </c:choose>
            </td>
            <td class="tw-py-6">
                <a href="${URL_PREFIX}/application/${app.id}" class="tw-block tw-mb-1 tw-text-lg print:no-link ${app.vacationType.category}">
                    <spring:message code="${app.vacationType.messageKey}"/>
                </a>
                <div>
                    <c:choose>
                        <c:when test="${app.startDate == app.endDate}">
                            <spring:message code="${app.weekDayOfStartDate}.short"/>,
                            <uv:date date="${app.startDate}"/>,
                            <c:choose>
                                <c:when test="${app.startTime != null && app.endTime != null}">
                                    <c:set var="APPLICATION_START_TIME">
                                        <uv:time dateTime="${app.startDateWithTime}"/>
                                    </c:set>
                                    <c:set var="APPLICATION_END_TIME">
                                        <uv:time dateTime="${app.endDateWithTime}"/>
                                    </c:set>
                                    <spring:message code="absence.period.time"
                                                    arguments="${APPLICATION_START_TIME};${APPLICATION_END_TIME}"
                                                    argumentSeparator=";"/>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="${app.dayLength}"/>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <uv:date-range from="${app.startDate}" to="${app.endDate}" pattern="E, dd.MM.yyyy" />
                        </c:otherwise>
                    </c:choose>
                </div>
            </td>
            <td class="is-centered">
                <span>
                    <c:choose>
                        <c:when test="${app.vacationType.category == 'OVERTIME'}">
                            <uv:duration duration="${app.hours}"/> <spring:message code="duration.overtime"/>
                        </c:when>
                        <c:otherwise>
                            <uv:number number="${app.workDays}"/> <spring:message code="duration.days"/>
                        </c:otherwise>
                    </c:choose>
                </span>

                <c:if test="${app.startDate.year != app.endDate.year}">
                    <span class="days-${loopStatus.index}">
                        <%--is filled by javascript--%>
                    </span>
                    <script type="text/javascript">
                        document.addEventListener('DOMContentLoaded', function() {
                          const dayLength = '<c:out value="${app.dayLength}" />';
                          const personId = '<c:out value="${app.person.id}" />';

                          <fmt:parseDate value="${app.startDate}" pattern="yyyy-MM-dd" var="parsedStartDate" type="date" />
                          <fmt:parseDate value="${app.endDate}" pattern="yyyy-MM-dd" var="parsedEndDate" type="date" />
                          const startDate = "<fmt:formatDate value="${parsedStartDate}" type="date" pattern="yyyy-MM-dd" />";
                          const endDate = "<fmt:formatDate value="${parsedEndDate}" type="date" pattern="yyyy-MM-dd" />";

                          sendGetDaysRequestForTurnOfTheYear("<spring:url value='/api' />", new Date(startDate), new Date(endDate), dayLength, personId, ".days-${loopStatus.index}");
                        })
                    </script>
                </c:if>
            </td>
            <td class="is-centered hidden-xs print:tw-hidden">
                <div class="tw-flex tw-items-center">
                    <icon:clock className="tw-w-4 tw-h-4" />&nbsp;
                    <span>
                        <c:choose>
                            <c:when test="${app.status == 'WAITING'}">
                                <spring:message code="application.progress.APPLIED"/> <uv:date date="${app.applicationDate}"/>
                            </c:when>
                            <c:when test="${app.status == 'TEMPORARY_ALLOWED'}">
                                <spring:message code="application.progress.TEMPORARY_ALLOWED"/> <uv:date date="${app.editedDate}"/>
                            </c:when>
                            <c:when test="${app.status == 'ALLOWED'}">
                                <c:choose>
                                    <c:when test="${app.editedDate != null}">
                                        <spring:message code="application.progress.ALLOWED"/> <uv:date date="${app.editedDate}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="application.progress.ALLOWED_DIRECTLY"/> <uv:date date="${app.applicationDate}"/>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:when test="${app.status == 'ALLOWED_CANCELLATION_REQUESTED'}">
                                <spring:message code="application.progress.ALLOWED_CANCELLATION_REQUESTED"/> <uv:date date="${app.cancelDate}"/>
                            </c:when>
                            <c:when test="${app.status == 'REJECTED'}">
                                <spring:message code="application.progress.REJECTED"/> <uv:date date="${app.editedDate}"/>
                            </c:when>
                            <c:when test="${app.status == 'CANCELLED' || app.status == 'REVOKED'}">
                                <spring:message code="application.progress.CANCELLED"/> <uv:date date="${app.cancelDate}"/>
                            </c:when>
                        </c:choose>
                    </span>
                </div>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
