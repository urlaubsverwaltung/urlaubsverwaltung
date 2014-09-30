<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<c:choose>

    <c:when test="${empty applications}">

        <spring:message code="no.apps"/>

    </c:when>

    <c:otherwise>

        <table class="list-table" cellspacing="0">
            <tbody>
            <c:forEach items="${applications}" var="app" varStatus="loopStatus">
                <c:choose>
                    <c:when test="${app.status.state == 'state.cancelled' || app.status.state == 'state.rejected'}">
                        <c:set var="CSS_CLASS" value="inactive"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="CSS_CLASS" value="active"/>
                    </c:otherwise>
                </c:choose>
                <tr class="${CSS_CLASS}">
                    <td class="is-centered state ${app.status}">
                        <span class="print--visible">
                            <spring:message code="${app.status.state}"/>
                        </span>
                        <span class="print--invisible">
                             <c:choose>
                                 <c:when test="${app.status == 'WAITING'}">
                                     <i class="fa fa-question"></i>
                                 </c:when>
                                 <c:when test="${app.status == 'ALLOWED'}">
                                     <i class="fa fa-check"></i>
                                 </c:when>
                                 <c:when test="${app.status == 'REJECTED'}">
                                     <i class="fa fa-ban"></i>
                                 </c:when>
                                 <c:when test="${app.status == 'CANCELLED'}">
                                     <i class="fa fa-trash"></i>
                                 </c:when>
                                 <c:otherwise>
                                     &nbsp;
                                 </c:otherwise>
                             </c:choose>
                        </span>
                    </td>
                    <td>
                        <a class="vacation ${app.vacationType}" href="${formUrlPrefix}/application/${app.id}">
                            <h4><spring:message code="${app.vacationType.vacationTypeName}"/></h4>
                        </a>

                        <p>
                            <c:choose>
                                <c:when test="${app.startDate == app.endDate}">
                                    <uv:date date="${app.startDate}"/>, <spring:message
                                        code="${app.howLong.dayLength}"/>
                                </c:when>
                                <c:otherwise>
                                    <uv:date date="${app.startDate}"/> - <uv:date date="${app.endDate}"/>
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </td>
                    <td class="is-centered hidden-xs">
                        <span><fmt:formatNumber maxFractionDigits="1" value="${app.days}" /> Tage</span>
                    </td>
                    <td>
                        <span class="hidden-xs hidden-sm">
                            <img class="box-image img-circle print--invisible" src="<c:out value='${gravatarUrls[app]}?d=mm&s=80'/>"/>
                            <i class="fa fa-at"></i> <c:out value="${app.person.loginName}"/>
                            <h4><c:out value="${app.person.niceName}"/></h4>
                            <i class="fa fa-envelope"></i> <c:out value="${app.person.email}"/>
                        </span>
                        <span class="visible-xs visible-sm">
                            <img class="box-image img-circle print--invisible" src="<c:out value='${gravatarUrls[app]}?d=mm&s=60'/>"/>
                            <span><c:out value="${app.person.niceName}"/></span>
                        </span>
                    </td>

                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>  

