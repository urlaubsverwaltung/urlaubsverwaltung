<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:if test="${not empty comments}">

    <uv:section-heading>
        <h2>
            <spring:message code="application.progress.title"/>
        </h2>
    </uv:section-heading>

    <table class="list-table striped-table bordered-table tw-text-sm">
        <tbody>

        <c:forEach items="${comments}" var="comment">
            <tr>
                <td class="print:tw-hidden tw-text-blue-50 dark:tw-text-sky-800">
                    <img
                        src="<c:out value='${comment.person.gravatarURL}?d=404&s=40'/>"
                        alt="<spring:message code="gravatar.alt" arguments="${comment.person.niceName}"/>"
                        class="gravatar gravatar--medium tw-rounded-full"
                        width="40px"
                        height="40px"
                        data-fallback="<c:out value="${URL_PREFIX}/avatar?name=${comment.person.niceName}"/>"
                        is="uv-avatar"
                    />
                </td>
                <td>
                    <c:out value="${comment.person.niceName}"/>
                </td>
                <td>
                    <p class="tw-m-0 tw-mb-1">
                        <spring:message code="application.progress.${comment.action}"/>

                        <c:choose>
                            <c:when test="${comment.action == 'APPLIED'}">
                                <uv:date date="${application.applicationDate}"/>
                            </c:when>
                            <c:when
                                test="${comment.action == 'ALLOWED' || comment.action == 'EDITED' || comment.action == 'TEMPORARY_ALLOWED' || comment.action == 'REJECTED' || comment.action == 'CONVERTED'}">
                                <uv:date date="${application.editedDate}"/>
                            </c:when>
                            <c:when test="${comment.action == 'ALLOWED_DIRECTLY'}">
                                <uv:date date="${application.applicationDate}"/>
                            </c:when>
                            <c:when test="${comment.action == 'CANCELLED' || comment.action == 'CANCELLED_DIRECTLY' || comment.action == 'REVOKED'}">
                                <uv:date date="${application.cancelDate}"/>
                            </c:when>
                            <c:when test="${comment.action == 'REFERRED' || comment.action == 'CANCEL_REQUESTED' || comment.action == 'CANCEL_REQUESTED_DECLINED'}">
                                <uv:instant date="${comment.date}"/>
                            </c:when>
                        </c:choose>

                        <c:if test="${comment.text != null && not empty comment.text}">
                            <spring:message code="application.progress.comment"/>
                        </c:if>
                    </p>

                    <c:if test="${comment.text != null && not empty comment.text}">
                    <p class="tw-m-0 tw-italic">
                        <c:out value="${comment.text}"/>
                    </p>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>
