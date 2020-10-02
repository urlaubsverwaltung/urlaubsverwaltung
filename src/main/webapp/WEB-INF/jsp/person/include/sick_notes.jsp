<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<table class="list-table selectable-table tw-text-sm">
    <tbody>
    <c:forEach items="${sickNotes}" var="sickNote" varStatus="loopStatus">
    <c:choose>
        <c:when test="${sickNote.active}">
            <c:set var="CSS_CLASS" value="active"/>
        </c:when>
        <c:otherwise>
            <c:set var="CSS_CLASS" value="inactive"/>
        </c:otherwise>
    </c:choose>
    <tr class="${CSS_CLASS}" onclick="navigate('${URL_PREFIX}/sicknote/${sickNote.id}');">
        <td class="is-centered hidden-print state ${sickNote.sickNoteType}">
            <c:choose>
                <c:when test="${sickNote.sickNoteType.category == 'SICK_NOTE_CHILD'}">
                    <icon:child className="tw-w-6 tw-h-6" />
                </c:when>
                <c:otherwise>
                    <icon:medkit className="tw-w-6 tw-h-6" />
                </c:otherwise>
            </c:choose>
        </td>
        <td class="tw-py-6">
            <a href="${URL_PREFIX}/sicknote/${sickNote.id}" class="tw-block tw-mb-1 tw-text-lg print:no-link">
                <spring:message code="${sickNote.sickNoteType.messageKey}"/>
            </a>
            <div class="tw-flex tw-flex-col lg:tw-flex-row">
                <c:choose>
                    <c:when test="${sickNote.startDate == sickNote.endDate}">
                        <spring:message code="${sickNote.weekDayOfStartDate}.short"/>,
                        <uv:date date="${sickNote.startDate}"/>, <spring:message code="${sickNote.dayLength}"/>
                    </c:when>
                    <c:otherwise>
                        <spring:message code="${sickNote.weekDayOfStartDate}.short"/>, <uv:date date="${sickNote.startDate}"/>
                        -
                        <spring:message code="${sickNote.weekDayOfEndDate}.short"/>, <uv:date date="${sickNote.endDate}"/>
                    </c:otherwise>
                </c:choose>

                <c:if test="${sickNote.aubPresent == true}">
                    <span class="tw-flex tw-items-center">
                        <span class="tw-hidden lg:tw-inline">&nbsp;</span>
                        (<span class="tw-text-green-500 tw-flex tw-items-center"><icon:check className="tw-w-4 tw-h-4" /></span>
                        &nbsp;<spring:message code="sicknote.data.aub.short"/>)
                    </span>
                </c:if>
            </div>
        </td>
        <td class="is-centered hidden-xs">
            <span><uv:number number="${sickNote.workDays}"/> <spring:message code="duration.days"/></span>
        </td>
        <td class="hidden-print is-centered hidden-xs">
            <div class="tw-flex tw-items-center">
                <icon:clock className="tw-w-4 tw-h-4" />&nbsp;
                <span><spring:message code="sicknote.progress.lastEdited"/> <uv:date date="${sickNote.lastEdited}"/></span>
            </div>
        </td>
        </c:forEach>
    </tbody>
</table>
