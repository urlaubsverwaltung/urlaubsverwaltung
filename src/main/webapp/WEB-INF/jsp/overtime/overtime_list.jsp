<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<html>
<head>
    <uv:head/>
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="content">
    <div class="container">
        <div class="row">
            <div class="col-xs-12">
                <div class="feedback">
                    <c:if test="${overtimeRecord != null}">
                        <div class="alert alert-success">
                            <spring:message code="overtime.feedback.${overtimeRecord}"/>
                        </div>
                    </c:if>
                </div>

                <legend>
                    <spring:message code="overtime.title"/>
                    <span>
                        <a href="${URL_PREFIX}/overtime/new" class="fa-action pull-right" data-title="<spring:message code="action.overtime.new"/>">
                            <i class="fa fa-plus-circle"></i>
                        </a>
                    </span>
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
                                        <td>
                                            <a href="${URL_PREFIX}/overtime/${record.id}"><h4><spring:message code="overtime.title"/></h4></a>
                                            <p><uv:date date="${record.startDate}"/> - <uv:date date="${record.endDate}"/></p>
                                        </td>
                                        <td class="is-centered">
                                            <c:out value="${record.hours}" /> <spring:message code="overtime.data.hours"/>
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
