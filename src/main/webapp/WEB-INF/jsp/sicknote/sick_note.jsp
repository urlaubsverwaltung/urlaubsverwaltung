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

<div class="print-info--only-portrait">
    <h4><spring:message code="print.info.portrait" /></h4>
</div>

<div class="content print--only-portrait">

    <div class="container">

        <div class="row">

            <div class="col-xs-12 col-sm-12 col-md-6">

                <legend>
                    <spring:message code="sicknote.title" />
                    <sec:authorize access="hasRole('OFFICE')">
                      <c:if test="${sickNote.active}">
                        <uv:print/>
                        <a href="#modal-cancel" role="button" data-toggle="modal" class="fa-action pull-right"
                           data-title="<spring:message code="action.delete"/>">
                          <i class="fa fa-trash"></i>
                        </a>
                        <a href="${URL_PREFIX}/sicknote/${sickNote.id}/convert" class="fa-action pull-right"
                           data-title="<spring:message code="action.convert"/>">
                          <i class="fa fa-retweet"></i>
                        </a>
                        <a href="${URL_PREFIX}/sicknote/${sickNote.id}/edit" class="fa-action pull-right"
                           data-title="<spring:message code="action.edit"/>">
                          <i class="fa fa-pencil"></i>
                        </a>
                      </c:if>
                    </sec:authorize>
                </legend>

                <form:form method="POST" action="${URL_PREFIX}/sicknote/${sickNote.id}/cancel">
                <div id="modal-cancel" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-remove"></i></button>
                                <h4 id="myModalLabel" class="modal-title"><spring:message code="action.delete" />?</h4>
                            </div>
                            <div class="modal-body">
                                <spring:message code="action.sicknote.cancel.confirm" />
                            </div>
                            <div class="modal-footer">
                                <button class="btn btn-danger is-sticky" type="submit"><spring:message code="action.delete" /></button>
                                <button class="btn btn-default is-sticky" data-dismiss="modal" aria-hidden="true"><spring:message code="action.cancel" /></button>
                            </div>
                        </div>
                        </div>
                    </div>
                </form:form>

                <div class="box">
                    <span class="box-icon bg-red hidden-print">
                        <c:choose>
                            <c:when test="${sickNote.sickNoteType.category == 'SICK_NOTE_CHILD'}">
                                <i class="fa fa-child"></i>
                            </c:when>
                            <c:otherwise>
                                <i class="fa fa-medkit"></i>
                            </c:otherwise>
                        </c:choose>
                    </span>
                    <span class="box-text">
                        <h5 class="is-inline-block is-sticky"><c:out value="${sickNote.person.niceName}"/></h5>
                        <spring:message code="sicknotes.details.title" arguments="${sickNote.sickNoteType.displayName}"/>

                        <c:choose>
                            <c:when test="${sickNote.startDate == sickNote.endDate}">
                                <c:set var="SICK_NOTE_DATE">
                                    <spring:message code="${sickNote.weekDayOfStartDate}.short"/>,
                                    <h5 class="is-inline-block is-sticky"><uv:date date="${sickNote.startDate}"/></h5>
                                </c:set>
                                <c:set var="SICK_NOTE_DAY_LENGTH">
                                    <spring:message code="${sickNote.dayLength}"/>
                                </c:set>
                                <spring:message code="absence.period.singleDay" arguments="${SICK_NOTE_DATE};${SICK_NOTE_DAY_LENGTH}" argumentSeparator=";"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="SICK_NOTE_START_DATE">
                                    <h5 class="is-inline-block is-sticky">
                                        <spring:message code="${sickNote.weekDayOfStartDate}.short"/>,
                                        <uv:date date="${sickNote.startDate}"/>
                                    </h5>
                                </c:set>
                                <c:set var="SICK_NOTE_END_DATE">
                                    <h5 class="is-inline-block is-sticky">
                                        <spring:message code="${sickNote.weekDayOfEndDate}.short"/>,
                                        <uv:date date="${sickNote.endDate}"/>
                                    </h5>
                                </c:set>
                                <spring:message code="absence.period.multipleDays" arguments="${SICK_NOTE_START_DATE};${SICK_NOTE_END_DATE}" argumentSeparator=";"/>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>

                <table class="list-table striped-table bordered-table">
                    <tbody>
                    <tr>
                        <td>
                            <spring:message code="absence.period.duration"/>
                        </td>
                        <td>
                            = <uv:number number="${sickNote.workDays}"/> <spring:message
                                code="duration.workDays"/>
                            <c:if test="${sickNote.active == false}">
                                <span><spring:message code="sicknote.data.inactive" /></span>
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <td><spring:message code="sicknote.data.aub.short"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${sickNote.aubPresent}">
                                    <i class="fa fa-check hidden-print"></i>
                                    <uv:date date="${sickNote.aubStartDate}"/> - <uv:date
                                        date="${sickNote.aubEndDate}"/>
                                </c:when>
                                <c:otherwise>
                                    <i class="fa fa-remove hidden-print"></i>
                                    <spring:message code="sicknote.data.aub.notPresent"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    </tbody>
                </table>

            </div><%-- End of first column --%>

            <div class="col-xs-12 col-sm-12 col-md-6">
                
                <legend>
                    <spring:message code="sicknote.progress.title" />
                    <sec:authorize access="hasRole('OFFICE')">
                      <a href="#" class="fa-action pull-right" onclick="$('div#comment-form').show();"
                         data-title="<spring:message code="action.comment.new" />">
                        <i class="fa fa-comments"></i>
                      </a>
                    </sec:authorize>
                </legend>

                <table class="list-table striped-table bordered-table">
                    <tbody>
                        <c:forEach items="${comments}" var="comment" varStatus="loopStatus">
                            <tr>
                                <td class="hidden-print">
                                    <div class="gravatar gravatar--medium img-circle center-block" data-gravatar="<c:out value='${comment.person.gravatarURL}?d=mm&s=40'/>"></div>
                                </td>
                                <td>
                                    <c:out value="${comment.person.niceName}" />
                                </td>
                                <td>
                                    <uv:date date="${comment.date}" />:
                                    <br />
                                    <c:choose>
                                        <c:when test="${empty comment.text}">
                                            <spring:message code="sicknote.progress.${comment.action}" />
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
                            <span id="text-comment"></span><spring:message code="action.comment.maxChars" />
                            <form:textarea rows="2" path="text" cssClass="form-control" cssErrorClass="form-control error" onkeyup="count(this.value, 'text-comment');" onkeydown="maxChars(this,200); count(this.value, 'text-comment');" />
                            <br />
                            <button class="btn btn-success col-xs-12 col-sm-5" type="submit">
                                <spring:message code="action.save" />
                            </button>
                            <button class="btn btn-default col-xs-12 col-sm-5 pull-right" type="button" onclick="$('div#comment-form').hide();">
                                <spring:message code="action.cancel" />
                            </button>
                        </form:form> 
                    </div>
                
                </sec:authorize>

                <legend class="hidden-print">
                    <spring:message code="sicknote.data.staff" />
                </legend>
                
                <uv:person person="${sickNote.person}" cssClass="hidden-print"/>
            </div><%-- End of second column --%>

        </div><%-- End of row --%>

    </div><%-- End of container --%>

</div><%-- End of content --%>

</body>
</html>