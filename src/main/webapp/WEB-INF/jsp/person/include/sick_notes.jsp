<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<table class="list-table bordered-table" cellspacing="0">
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
    <tr class="${CSS_CLASS}" onclick="navigate('${formUrlPrefix}/sicknote/${sickNote.id}');">
        <td class="is-centered state">
            <span class="hidden-print">
                <c:choose>
                    <c:when test="${sickNote.type == 'SICK_NOTE_CHILD'}">
                        <i class="fa fa-child"></i>
                    </c:when>
                    <c:otherwise>
                        <i class="fa fa-medkit"></i>
                    </c:otherwise>
                </c:choose>
            </span>
        </td>
        <td>
            <a href="${formUrlPrefix}/sicknote/${sickNote.id}" class="hidden-print">
                <h4>
                    <spring:message code="sicknote" />
                </h4>
            </a>

            <h4 class="visible-print">
                <spring:message code="sicknote" />
            </h4>

            <p>
                <uv:date date="${sickNote.startDate}"/> - <uv:date date="${sickNote.endDate}"/>

                <c:if test="${sickNote.aubPresent == true}">
                    (<i class="fa fa-check check"></i> <spring:message code="sicknotes.aub.short" />)
                </c:if>
            </p>
        </td>
        <td class="is-centered">
            <span><fmt:formatNumber maxFractionDigits="1" value="${sickNote.workDays}"/> Tage</span>
        </td>
        <td class="hidden-print is-centered hidden-xs">
            <i class="fa fa-clock-o"></i> <span><spring:message code="sicknote.lastEdited" /> <uv:date date="${sickNote.lastEdited}"/></span>
        </td>
        </c:forEach>
    </tbody>
</table>
