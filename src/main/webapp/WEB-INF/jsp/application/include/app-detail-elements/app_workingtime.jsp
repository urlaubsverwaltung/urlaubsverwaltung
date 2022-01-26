<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<uv:section-heading>
    <h2>
        <spring:message code="application.data.workingTime"/>
    </h2>
</uv:section-heading>

<div class="tw-px-3">
    <table class="tw-w-full tw-text-sm tw-mb-8">
        <caption class="tw-sr-only">
            <spring:message code="person.account.workingTime.title"/>
        </caption>
        <thead>
        <tr>
            <th class="tw-pb-1 tw-w-5 tw-font-medium" scope="col">
                <spring:message code="person.account.workingTime.validity"/>
            </th>
            <th class="tw-pb-1 text-center tw-w-4 tw-font-medium" scope="col">
                <spring:message code="MONDAY.short"/>
            </th>
            <th class="tw-pb-1 text-center tw-w-4 tw-font-medium" scope="col">
                <spring:message code="TUESDAY.short"/>
            </th>
            <th class="tw-pb-1 text-center tw-w-4 tw-font-medium" scope="col">
                <spring:message code="WEDNESDAY.short"/>
            </th>
            <th class="tw-pb-1 text-center tw-w-4 tw-font-medium" scope="col">
                <spring:message code="THURSDAY.short"/>
            </th>
            <th class="tw-pb-1 text-center tw-w-4 tw-font-medium" scope="col">
                <spring:message code="FRIDAY.short"/>
            </th>
            <th class="tw-pb-1 text-center tw-w-4 tw-font-medium" scope="col">
                <spring:message code="SATURDAY.short"/>
            </th>
            <th class="tw-pb-1 text-center tw-w-4 tw-font-medium" scope="col">
                <spring:message code="SUNDAY.short"/>
            </th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${dateRangeWorkingTimes}" var="dateRangeWorkingTime">
            <tr>
                <th class="tw-font-normal" scope="row">
                    <uv:date date="${dateRangeWorkingTime.key.startDate}" pattern="dd.MM.YYYY" />
                </th>
                <td class="tw-py-0.5 text-center">
                    <c:if test="${dateRangeWorkingTime.value.monday.duration > 0}">
                        <icon:check-circle />
                    </c:if>
                    <span class="tw-sr-only">
                        <spring:message code="${dateRangeWorkingTime.value.monday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                    </span>
                </td>
                <td class="tw-py-0.5 text-center">
                    <c:if test="${dateRangeWorkingTime.value.tuesday.duration > 0}">
                        <icon:check-circle />
                    </c:if>
                    <span class="tw-sr-only">
                        <spring:message code="${dateRangeWorkingTime.value.tuesday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                    </span>
                </td>
                <td class="tw-py-0.5 text-center">
                    <c:if test="${dateRangeWorkingTime.value.wednesday.duration > 0}">
                        <icon:check-circle />
                    </c:if>
                    <span class="tw-sr-only">
                        <spring:message code="${dateRangeWorkingTime.value.wednesday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                    </span>
                </td>
                <td class="tw-py-0.5 text-center">
                    <c:if test="${dateRangeWorkingTime.value.thursday.duration > 0}">
                        <icon:check-circle />
                    </c:if>
                    <span class="tw-sr-only">
                        <spring:message code="${dateRangeWorkingTime.value.thursday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                    </span>
                </td>
                <td class="tw-py-0.5 text-center">
                    <c:if test="${dateRangeWorkingTime.value.friday.duration > 0}">
                        <icon:check-circle />
                    </c:if>
                    <span class="tw-sr-only">
                        <spring:message code="${dateRangeWorkingTime.value.friday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                    </span>
                </td>
                <td class="tw-py-0.5 text-center">
                    <c:if test="${dateRangeWorkingTime.value.saturday.duration > 0}">
                        <icon:check-circle />
                    </c:if>
                    <span class="tw-sr-only">
                        <spring:message code="${dateRangeWorkingTime.value.saturday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                    </span>
                </td>
                <td class="tw-py-0.5 text-center">
                    <c:if test="${dateRangeWorkingTime.value.sunday.duration > 0}">
                        <icon:check-circle />
                    </c:if>
                    <span class="tw-sr-only">
                        <spring:message code="${dateRangeWorkingTime.value.sunday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                    </span>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
