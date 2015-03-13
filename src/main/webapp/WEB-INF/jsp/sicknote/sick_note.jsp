<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<html>
<head>
    <uv:head />
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu />

<div class="content">

    <div class="container">

        <div class="row">

            <div class="col-xs-12 col-sm-12 col-md-6">

                <div class="header">
                    <legend>
                        <p><spring:message code="sicknote" /></p>
                        <sec:authorize access="hasRole('OFFICE')">
                            <c:if test="${sickNote.active}">
                                <uv:print />
                                <div class="btn-group pull-right">
                                    <a class="btn btn-default dropdown-toggle" data-toggle="dropdown" href="#">
                                        <i class="fa fa-edit"></i>
                                        <span class="hidden-xs">
                                            <spring:message code="action" />
                                        </span>
                                        <span class="caret"></span>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li>
                                            <a href="${URL_PREFIX}/sicknote/${sickNote.id}/edit">
                                                <i class="fa fa-pencil"></i>&nbsp;&nbsp;<spring:message code="action.edit" />
                                            </a>
                                        </li>
                                        <li>
                                            <a href="${URL_PREFIX}/sicknote/${sickNote.id}/convert">
                                                <i class="fa fa-retweet"></i>&nbsp;&nbsp;<spring:message code="sicknotes.convert.vacation.short" />
                                            </a>
                                        </li>
                                        <li>
                                            <a href="#modal-cancel" role="button" data-toggle="modal">
                                                <i class="fa fa-trash"></i>&nbsp;&nbsp;<spring:message code="action.delete" />
                                            </a>
                                        </li>
                                    </ul>
                                </div>
                            </c:if>
                        </sec:authorize>
                    </legend>
                </div>

                <form:form method="POST" action="${URL_PREFIX}/sicknote/${sickNote.id}/cancel">
                <div id="modal-cancel" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-remove"></i></button>
                                <h4 id="myModalLabel" class="modal-title"><spring:message code="action.delete" />?</h4>
                            </div>
                            <div class="modal-body">
                                <spring:message code="sicknote.cancel" />
                            </div>
                            <div class="modal-footer">
                                <button class="btn btn-danger is-sticky" type="submit"><i class="fa fa-trash"></i>&nbsp;<spring:message code="action.delete" /></button>
                                <button class="btn btn-default is-sticky" data-dismiss="modal" aria-hidden="true"><i class="fa fa-remove"></i>&nbsp;<spring:message code="action.cancel" /></button>
                            </div>
                        </div>
                        </div>
                    </div>
                </form:form>

                <div class="box">
                    <span class="box-icon bg-red">
                        <c:choose>
                            <c:when test="${sickNote.type == 'SICK_NOTE_CHILD'}">
                                <i class="fa fa-child"></i>
                            </c:when>
                            <c:otherwise>
                                <i class="fa fa-medkit"></i>
                            </c:otherwise>
                        </c:choose>
                    </span>
                    <span class="box-text">
                        <h5 class="is-inline-block is-sticky"><c:out value="${sickNote.person.niceName}"/></h5>
                        <c:choose>
                            <c:when test="${sickNote.type == 'SICK_NOTE_CHILD'}">
                                <spring:message code="sicknotes.details.title.child" />
                            </c:when>
                            <c:otherwise>
                                <spring:message code="sicknotes.details.title" />
                            </c:otherwise>
                        </c:choose>

                        <c:choose>
                            <c:when test="${sickNote.startDate == sickNote.endDate}">
                                <spring:message code="at"/> <h5 class="is-inline-block is-sticky"><uv:date date="${sickNote.startDate}"/></h5>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="from"/> <h5 class="is-inline-block is-sticky"><uv:date
                                    date="${sickNote.startDate}"/></h5> <spring:message code="to"/> <h5 class="is-inline-block is-sticky">
                                <uv:date date="${sickNote.endDate}"/></h5>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>

                <table class="list-table striped-table bordered-table">
                    <tbody>
                    <tr>
                        <td>
                            <spring:message code="days.time"/>
                        </td>
                        <td>
                            = <uv:number number="${sickNote.workDays}"/> <spring:message
                                code="work.days"/>
                            <c:if test="${sickNote.active == false}">
                                <span><spring:message code="sicknotes.details.inactive" /></span>
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <td><spring:message code="sicknotes.aub.short"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${sickNote.aubPresent}">
                                    <i class="fa fa-check hidden-print"></i>
                                    <uv:date date="${sickNote.aubStartDate}"/> - <uv:date
                                        date="${sickNote.aubEndDate}"/>
                                </c:when>
                                <c:otherwise>
                                    <i class="fa fa-remove hidden-print"></i>
                                    <spring:message code="no"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    </tbody>
                </table>

            </div><%-- End of first column --%>

            <div class="col-xs-12 col-sm-12 col-md-6">

                <div class="header">
                    <legend>
                        <p><spring:message code="progress" /></p>
                        <sec:authorize access="hasRole('OFFICE')">
                            <button class="btn btn-default pull-right" onclick="$('div#comment-form').show();">
                                <i class="fa fa-comment"></i> <span class="hidden-xs"><spring:message code="sicknotes.comment.new" /></span>
                            </button>
                        </sec:authorize>
                    </legend>
                </div>
                
                <table class="list-table striped-table bordered-table">
                    <tbody>
                        <c:forEach items="${comments}" var="comment" varStatus="loopStatus">
                            <tr>
                                <td>
                                    <img class="img-circle hidden-print center-block" src="<c:out value='${gravatarUrls[comment]}?d=mm&s=40'/>"/>
                                </td>
                                <td>
                                    <c:out value="${comment.person.niceName}" />
                                </td>
                                <td>
                                    <uv:date date="${comment.date}" />:
                                    <br />
                                    <c:choose>
                                        <c:when test="${empty comment.text}">
                                            <spring:message code="sicknote.${comment.status}" /> 
                                        </c:when>
                                        <c:otherwise>
                                            <c:out value="${comment.text}" />
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>

                <sec:authorize access="hasRole('OFFICE')">
                
                    <c:choose>
                        <c:when test="${not empty errors}">
                           <c:set var="STYLE" value="display: block" />
                            <div class="feedback">
                                <div class="alert alert-danger">
                                    <spring:message code="application.action.reason.error" />
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:set var="STYLE" value="display: none" /> 
                        </c:otherwise>
                    </c:choose>
                    
                    <div id="comment-form" style="${STYLE}">
                        <form:form method="POST" action="${URL_PREFIX}/sicknote/${sickNote.id}/comment" modelAttribute="comment">
                            <span id="text-comment"></span><spring:message code="max.chars" />
                            <form:textarea rows="2" path="text" cssClass="form-control" cssErrorClass="form-control error" onkeyup="count(this.value, 'text-comment');" onkeydown="maxChars(this,200); count(this.value, 'text-comment');" />
                            <br />
                            <button class="btn btn-success col-xs-12 col-sm-5" type="submit">
                                <i class="fa fa-check"></i>&nbsp;<spring:message code="action.save" />
                            </button>
                            <button class="btn btn-default col-xs-12 col-sm-5 pull-right" type="button" onclick="$('div#comment-form').hide();">
                                <i class="fa fa-remove"></i>&nbsp;<spring:message code="action.cancel" />
                            </button>
                        </form:form> 
                    </div>
                
                </sec:authorize>

                <div class="header hidden-print">
                    <legend>
                        <p><spring:message code="staff" /></p>
                    </legend>
                </div>

                <div class="box hidden-print">
                    <img class="box-image img-circle" src="<c:out value='${gravatar}?d=mm&s=60'/>"/>
                    <span class="box-text">
                        <i class="fa fa-at"></i> <c:out value="${sickNote.person.loginName}"/>
                        <h4>
                            <a href="${URL_PREFIX}/staff/${sickNote.person.id}/overview">
                                <c:out value="${sickNote.person.niceName}"/>
                            </a>
                        </h4>
                        <i class="fa fa-envelope"></i> <c:out value="${sickNote.person.email}"/>
                    </span>
                </div>
                
            </div><%-- End of second column --%>

        </div><%-- End of row --%>

    </div><%-- End of container --%>

</div><%-- End of content --%>

</body>
</html>