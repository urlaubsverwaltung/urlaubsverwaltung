<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<html>
<head>
    <uv:head/>
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<sec:authorize access="hasRole('OFFICE')">
    <c:set var="IS_OFFICE" value="${true}"/>
</sec:authorize>

<uv:menu/>

<div class="content">
    <div class="container">
        <div class="row">
            <div class="col-xs-12">
                <legend>
                    <spring:message code="overtime.title"/>
                    <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/overtime?person=${person.id}&year="/>

                    <c:if test="${IS_OFFICE || signedInUser.id == person.id}">
                        <a href="${URL_PREFIX}/overtime/new?person=${person.id}" class="fa-action pull-right" data-title="<spring:message code="action.overtime.new"/>">
                            <i class="fa fa-plus-circle"></i>
                        </a>
                    </c:if>
                </legend>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-4">
                <uv:person person="${person}"/>
            </div>
            <div class="col-xs-12 col-md-4">
                <uv:overtime-total hours="${overtimeTotal}"/>
            </div>
            <div class="col-xs-12 col-md-4">
                <uv:overtime-left hours="${overtimeLeft}"/>
            </div>

            <div class="col-xs-12">
                <legend>
                    <spring:message code="overtime.list"/>
                </legend>
                <c:choose>
                    <c:when test="${empty records}">
                        <p><spring:message code="overtime.none"/></p>
                    </c:when>
                    <c:otherwise>
                        <table class="list-table bordered-table selectable-table" cellspacing="0">
                            <tbody>
                            <c:forEach items="${records}" var="record">
                                <tr onclick="navigate('${URL_PREFIX}/overtime/${record.id}');">
                                    <td class="is-centered state">
                                        <span class="hidden-print"><i class="fa fa-history"></i></span>
                                    </td>
                                    <td>
                                        <h4 class="visible-print">
                                            <spring:message code="overtime.title" />
                                        </h4>
                                        <a class="hidden-print" href="${URL_PREFIX}/overtime/${record.id}">
                                            <h4><spring:message code="overtime.title"/></h4>
                                        </a>
                                        <p><uv:date date="${record.startDate}"/> - <uv:date date="${record.endDate}"/></p>
                                    </td>
                                    <td class="is-centered hidden-xs">
                                        <uv:number number="${record.hours}"/>
                                        <spring:message code="duration.hours"/>
                                    </td>
                                    <td class="hidden-print is-centered hidden-xs">
                                        <i class="fa fa-clock-o"></i>
                                        <spring:message code="overtime.progress.lastEdited"/>
                                        <uv:date date="${record.lastModificationDate}"/>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>

        </div><%-- End of row --%>
    </div><%-- End of container --%>
</div><%-- End of content --%>

</body>
</html>
