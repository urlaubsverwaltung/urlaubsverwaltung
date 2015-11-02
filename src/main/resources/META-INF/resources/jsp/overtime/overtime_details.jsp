<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<html>
<head>
    <uv:head/>
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<sec:authorize access="hasRole('OFFICE')">
    <c:set var="IS_OFFICE" value="true"/>
</sec:authorize>

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
            </div>
            <div class="col-xs-12 col-md-6">
                <legend>
                    <spring:message code="overtime.title"/>
                    <c:if test="${record.person.id == signedInUser.id || IS_OFFICE}">
                        <a href="${URL_PREFIX}/overtime/${record.id}/edit" class="fa-action pull-right"
                           data-title="<spring:message code="action.edit"/>">
                            <i class="fa fa-pencil"></i>
                        </a>
                    </c:if>
                </legend>
                <div class="box">
                    <span class="box-icon bg-green">
                        <i class="fa fa-history"></i>
                    </span>
                    <span class="box-text">
                        <h5 class="is-inline-block is-sticky"><c:out value="${record.person.niceName}"/></h5>
                        <spring:message code="overtime.details.hours" arguments="${record.hours}"/>
                        <c:set var="START_DATE">
                            <h5 class="is-inline-block is-sticky"><uv:date date="${record.startDate}"/></h5>
                        </c:set>
                        <c:set var="END_DATE">
                            <h5 class="is-inline-block is-sticky"><uv:date date="${record.endDate}"/></h5>
                        </c:set>
                        <spring:message code="overtime.details.period" arguments="${START_DATE};${END_DATE}" argumentSeparator=";"/>
                    </span>
                </div>
                <legend>
                    <spring:message code="overtime.progress.title"/>
                </legend>
                <table class="list-table striped-table bordered-table">
                    <tbody>
                        <c:forEach items="${comments}" var="comment">
                           <tr>
                               <td>
                                   <div class="gravatar gravatar--medium img-circle hidden-print center-block" data-gravatar="<c:out value='${comment.person.gravatarURL}?d=mm&s=40'/>"></div>
                               </td>
                               <td>
                                   <c:out value="${comment.person.niceName}"/>
                               </td>
                               <td>
                                   <spring:message code="overtime.progress.${comment.action}"/>
                                   <uv:date date="${comment.date}"/>
                                   <c:if test="${comment.text != null && not empty comment.text}">
                                       <spring:message code="overtime.progress.comment"/>
                                       <br/>
                                       <i><c:out value="${comment.text}"/></i>
                                   </c:if>
                               </td>
                           </tr> 
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            <div class="col-xs-12 col-md-6">
                <legend class="hidden-print">
                    <spring:message code="overtime.data.staff"/>
                </legend>
                <uv:person person="${record.person}" cssClass="hidden-print"/>
                <uv:overtime-total hours="${overtimeTotal}"/>
                <uv:overtime-left hours="${overtimeLeft}"/>
            </div>
        </div><%-- End of row --%>
    </div><%-- End of container --%>
</div><%-- End of content --%>

</body>
</html>
