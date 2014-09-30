<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<c:choose>

    <c:when test="${empty sickNotes}">
        <p>
            <spring:message code="sicknotes.none" />
        </p>
    </c:when>

    <c:otherwise>
        <table class="list-table" cellspacing="0">
            <%--<thead>--%>
            <%--<tr>--%>
                <%--<th><spring:message code="sicknotes.time" /></th>--%>
                <%--<th><spring:message code="work.days" /></th>--%>
                <%--<th><spring:message code="sicknotes.aub.short" /></th>--%>
                <%--<th class="print--invisible"><spring:message code="app.date.overview" /></th>--%>
            <%--</tr>--%>
            <%--</thead>--%>
            <tbody>
            <c:forEach items="${sickNotes}" var="sickNote" varStatus="loopStatus">
                <c:choose>
                    <c:when test="${sickNote.active}">
                        <c:set var="CSS_CLASS" value="active" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="CSS_CLASS" value="inactive" />
                    </c:otherwise>
                </c:choose>
                <tr class="${CSS_CLASS}" onclick="navigate('${formUrlPrefix}/sicknote/${sickNote.id}');">
                    <td class="SICKNOTE">
                       <span class="print--visible">
                           <%--<spring:message code="${app.status.state}" />--%>
                           Krankheit
                       </span>
                       <span class="print--invisible">
                           <%--<c:choose>--%>
                               <%--<c:when test="${app.status == 'WAITING'}">--%>
                               <%--</c:when>--%>
                               <%--<c:otherwise></c:otherwise>--%>
                           <%--</c:choose>--%>
                           <i class="fa fa-medkit"></i> 
                       </span> 
                    </td>
                    <td>
                        <a href="${formUrlPrefix}/sicknote/${sickNote.id}">
                            <h4>
                                Krankmeldung/Kind-Krankmeldung
                                <%--<spring:message code="${app.vacationType.vacationTypeName}"/>--%>
                            </h4>
                        </a>
                        <p>
                            <uv:date date="${sickNote.startDate}" /> - <uv:date date="${sickNote.endDate}" />
                            
                            <c:if test="${sickNote.aubPresent == true}">
                                (AU liegt vor <i class="fa fa-check"></i>)  
                            </c:if>
                        </p>
                    </td>
                    <td>
                        <fmt:formatNumber maxFractionDigits="1" value="${sickNote.workDays}" /> Tage
                    </td>
                    <td class="print--invisible is-centered">
                        <i class="fa fa-clock-o"></i> zuletzt bearbeitet am <uv:date date="${sickNote.lastEdited}" />
                    </td>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>

</c:choose>
