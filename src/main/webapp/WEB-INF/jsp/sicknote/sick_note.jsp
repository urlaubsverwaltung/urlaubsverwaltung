<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}" class="tw-<c:out value='${theme}' />">

<c:set var="SICK_NOTE_MESSAGEKEY">
    <spring:message code='${sickNote.sickNoteType.messageKey}'/>
</c:set>

<head>
    <title>
        <spring:message code="sicknote.header.title" arguments="${SICK_NOTE_MESSAGEKEY}, ${sickNote.person.niceName}"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='sick_note.js' />"></script>
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="content">

    <div class="container">

        <div class="row">

            <div class="col-xs-12 col-sm-12 col-md-6">

                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <c:if test="${sickNote.active}">
                            <c:if test="${canEditSickNote}">
                            <a href="${URL_PREFIX}/sicknote/${sickNote.id}/edit" class="icon-link tw-px-1" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                            </c:if>
                            <c:if test="${canConvertSickNote}">
                            <a href="${URL_PREFIX}/sicknote/${sickNote.id}/convert" class="icon-link tw-px-1" data-title="<spring:message code="action.convert"/>">
                                <icon:refresh className="tw-w-5 tw-h-5" />
                            </a>
                            </c:if>
                            <c:if test="${canDeleteSickNote}">
                            <a href="#modal-cancel" role="button" data-toggle="modal" class="icon-link tw-px-1" data-title="<spring:message code="action.delete"/>">
                                <icon:trash className="tw-w-5 tw-h-5" />
                            </a>
                            </c:if>
                            <uv:print/>
                        </c:if>
                    </jsp:attribute>
                    <jsp:body>
                        <h1>
                            <spring:message code="sicknote.title"/>
                        </h1>
                    </jsp:body>
                </uv:section-heading>

                <form:form method="POST" action="${URL_PREFIX}/sicknote/${sickNote.id}/cancel">
                    <div id="modal-cancel" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
                         aria-hidden="true">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                        <icon:x-circle className="tw-w-8 tw-h-8" solid="true" />
                                    </button>
                                    <h4 id="myModalLabel" class="modal-title"><spring:message
                                        code="action.delete"/>?</h4>
                                </div>
                                <div class="modal-body">
                                    <spring:message code="action.sicknote.cancel.confirm"/>
                                </div>
                                <div class="modal-footer tw-flex tw-space-x-2 tw-justify-end">
                                    <button class="button-danger" type="submit"><spring:message
                                        code="action.delete"/></button>
                                    <button class="button" data-dismiss="modal" aria-hidden="true">
                                        <spring:message code="action.cancel"/></button>
                                </div>
                            </div>
                        </div>
                    </div>
                </form:form>

                <uv:box className="tw-mb-6">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-red-500 tw-text-white dark:tw-bg-red-600 dark:tw-text-zinc-900">
                            <c:choose>
                                <c:when test="${sickNote.sickNoteType.category == 'SICK_NOTE_CHILD'}">
                                    <icon:child className="tw-w-8 tw-h-8" />
                                </c:when>
                                <c:otherwise>
                                    <icon:medkit className="tw-w-8 tw-h-8" />
                                </c:otherwise>
                            </c:choose>
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100" data-test-id="sicknote-person">
                            <spring:message
                                code="sicknotes.details.box.person.has"
                                arguments="${sickNote.person.niceName}"
                            />
                        </span>
                        <span class="tw-my-1 tw-text-lg tw-font-medium" data-test-id="sicknote-type">
                            <c:out value="${SICK_NOTE_MESSAGEKEY}" />
                        </span>
                        <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100" data-test-id="sicknote-date">
                            <c:choose>
                                <c:when test="${sickNote.startDate == sickNote.endDate}">
                                    <c:set var="SICK_NOTE_DATE">
                                        <spring:message code="${sickNote.weekDayOfStartDate}.short"/>,
                                        <uv:date date="${sickNote.startDate}"/>
                                    </c:set>
                                    <c:set var="SICK_NOTE_DAY_LENGTH">
                                        <spring:message code="${sickNote.dayLength}"/>
                                    </c:set>
                                    <spring:message
                                        code="absence.period.singleDay"
                                        arguments="${SICK_NOTE_DATE};${SICK_NOTE_DAY_LENGTH}"
                                        argumentSeparator=";"
                                    />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="SICK_NOTE_START_DATE">
                                        <spring:message code="${sickNote.weekDayOfStartDate}.short"/>,
                                        <uv:date date="${sickNote.startDate}"/>
                                    </c:set>
                                    <c:set var="SICK_NOTE_END_DATE">
                                        <spring:message code="${sickNote.weekDayOfEndDate}.short"/>,
                                        <uv:date date="${sickNote.endDate}"/>
                                    </c:set>
                                    <spring:message
                                        code="absence.period.multipleDays"
                                        arguments="${SICK_NOTE_START_DATE};${SICK_NOTE_END_DATE}"
                                        argumentSeparator=";"
                                    />
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </jsp:body>
                </uv:box>

                <table class="list-table striped-table bordered-table tw-text-sm">
                    <tbody>
                    <tr>
                        <td>
                            <spring:message code="absence.period.duration"/>
                        </td>
                        <td>
                            = <uv:number number="${sickNote.workDays}"/> <spring:message
                            code="duration.workDays"/>
                            <c:if test="${sickNote.active == false}">
                                <span><spring:message code="sicknote.data.inactive"/></span>
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <td><spring:message code="sicknote.data.aub.short"/></td>
                        <td>
                            <div class="tw-flex tw-items-center" data-test-id="sicknote-aub-date">
                            <c:choose>
                                <c:when test="${sickNote.aubPresent}">
                                    <icon:check className="tw-w-4 tw-h-4" />
                                    &nbsp;<uv:date-range from="${sickNote.aubStartDate}" to="${sickNote.aubEndDate}" />
                                </c:when>
                                <c:otherwise>
                                    <icon:x className="tw-w-4 tw-h-4" />
                                    &nbsp;<spring:message code="sicknote.data.aub.notPresent"/>
                                </c:otherwise>
                            </c:choose>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>

            </div>
            <%-- End of first column --%>

            <div class="col-xs-12 col-sm-12 col-md-6">

                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <c:if test="${canCommentSickNote}">
                            <a href="#" class="icon-link tw-px-1" onclick="$('div#comment-form').show();" data-title="<spring:message code="action.comment.new" />">
                                <icon:annotation className="tw-w-5 tw-h-5" />
                            </a>
                        </c:if>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            <spring:message code="sicknote.progress.title"/>
                        </h2>
                    </jsp:body>
                </uv:section-heading>

                <table class="list-table striped-table bordered-table tw-text-sm">
                    <tbody>
                    <c:forEach items="${comments}" var="comment" varStatus="loopStatus">
                        <tr>
                            <td class="print:tw-hidden">
                                <img
                                    src="<c:out value='${comment.person.gravatarURL}?d=mm&s=40'/>"
                                    alt="<spring:message code="gravatar.alt" arguments="${comment.person.niceName}"/>"
                                    class="gravatar gravatar--medium tw-rounded-full"
                                    width="40px"
                                    height="40px"
                                    onerror="this.src !== '/images/gravatar.jpg' && (this.src = '/images/gravatar.jpg')"
                                />
                            </td>
                            <td>
                                <c:out value="${comment.person.niceName}"/>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${empty comment.text}">
                                        <spring:message code="sicknote.progress.${comment.action}"/>
                                        <uv:instant date="${comment.date}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="sicknote.progress.${comment.action}"/>
                                        <uv:instant date="${comment.date}"/>
                                        <c:choose>
                                            <c:when test="${comment.action == 'COMMENTED'}">
                                                :
                                            </c:when>
                                            <c:otherwise>
                                                <spring:message code="sicknote.progress.comment"/>
                                            </c:otherwise>
                                        </c:choose>
                                        <br/>
                                        <em><c:out value="${comment.text}"/></em>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <c:if test="${canCommentSickNote}">

                    <c:choose>
                        <c:when test="${not empty errors}">
                            <c:set var="STYLE" value="display: block"/>
                            <div class="feedback">
                                <div class="alert alert-danger">
                                    <spring:message code="application.action.reason.error"/>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:set var="STYLE" value="display: none"/>
                        </c:otherwise>
                    </c:choose>

                    <div id="comment-form" style="${STYLE}">
                        <form:form method="POST" action="${URL_PREFIX}/sicknote/${sickNote.id}/comment"
                                   modelAttribute="comment">
                            <small>
                                <span id="text-comment"></span> <spring:message code="action.comment.maxChars"/>
                            </small>
                            <form:textarea rows="2" path="text" cssClass="form-control"
                                           cssErrorClass="form-control error"
                                           onkeyup="count(this.value, 'text-comment');"
                                           onkeydown="maxChars(this,200); count(this.value, 'text-comment');"/>
                            <div class="tw-mt-2">
                                <button class="button-main-green col-xs-12 col-sm-5" type="submit">
                                    <spring:message code="action.save"/>
                                </button>
                                <button class="button col-xs-12 col-sm-5 pull-right" type="button"
                                        onclick="$('div#comment-form').hide();">
                                    <spring:message code="action.cancel"/>
                                </button>
                            </div>
                        </form:form>
                    </div>

                </c:if>

                <div class="print:tw-hidden">
                    <uv:section-heading>
                        <h2>
                            <spring:message code="sicknote.data.person"/>
                        </h2>
                    </uv:section-heading>
                    <uv:person person="${sickNote.person}" />
                </div>
            </div>
            <%-- End of second column --%>

        </div>
        <%-- End of row --%>

    </div>
    <%-- End of container --%>

</div>
</body>
</html>
