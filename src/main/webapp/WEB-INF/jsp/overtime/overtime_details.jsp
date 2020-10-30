<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<!DOCTYPE html>
<html lang="${language}">
<head>
    <title>
        <spring:message code="overtime.details.header.title"/>
    </title>
    <uv:custom-head/>
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<sec:authorize access="hasAuthority('OFFICE')">
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
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <c:if test="${record.person.id == signedInUser.id || IS_OFFICE}">
                            <a href="${URL_PREFIX}/overtime/${record.id}/edit" class="icon-link tw-px-1" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </c:if>
                    </jsp:attribute>
                    <jsp:body>
                        <h1>
                            <spring:message code="overtime.title"/>
                        </h1>
                    </jsp:body>
                </uv:section-heading>

                <uv:box className="tw-h-32 tw-mb-8">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-green-500 tw-text-white">
                            <icon:briefcase className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <span class="tw-text-sm">
                            <c:out value="${record.person.niceName}"/>&nbsp;<spring:message code="overtime.details.hours.1"/>
                        </span>
                        <span class="tw-my-1 tw-text-lg tw-font-medium">
                            <spring:message code="overtime.details.hours.2" arguments="${record.hours}"/>
                        </span>
                        <span class="tw-text-sm">
                            <c:set var="START_DATE">
                                <uv:date date="${record.startDate}"/>
                            </c:set>
                            <c:set var="END_DATE">
                                <uv:date date="${record.endDate}"/>
                            </c:set>
                            <spring:message
                                code="overtime.details.period"
                                arguments="${START_DATE};${END_DATE}"
                                argumentSeparator=";"
                            />
                        </span>
                    </jsp:body>
                </uv:box>

                <uv:section-heading>
                    <h2>
                        <spring:message code="overtime.progress.title"/>
                    </h2>
                </uv:section-heading>
                <table class="list-table striped-table bordered-table tw-mb-8 tw-text-sm">
                    <tbody>
                    <c:forEach items="${comments}" var="comment">
                        <tr>
                            <td>
                                <img
                                    src="<c:out value='${comment.person.gravatarURL}?d=mm&s=40'/>"
                                    alt="<spring:message code="gravatar.alt" arguments="${comment.person.niceName}"/>"
                                    class="gravatar gravatar--medium tw-rounded-full print:tw-hidden"
                                    width="40px"
                                    height="40px"
                                    onerror="this.src !== '/images/gravatar.jpg' && (this.src = '/images/gravatar.jpg')"
                                />
                            </td>
                            <td>
                                <c:out value="${comment.person.niceName}"/>
                            </td>
                            <td>
                                <spring:message code="overtime.progress.${comment.action}"/>
                                <uv:instant date="${comment.date}"/>
                                <c:if test="${comment.text != null && not empty comment.text}">
                                    <spring:message code="overtime.progress.comment"/>
                                    <br/>
                                    <em><c:out value="${comment.text}"/></em>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <div class="col-xs-12 col-md-6">
                <uv:section-heading>
                    <h2>
                        <spring:message code="overtime.data.person"/>
                    </h2>
                </uv:section-heading>
                <uv:person person="${record.person}" cssClass="hidden-print tw-h-32 tw-mb-5"/>
                <uv:overtime-total hours="${overtimeTotal}" cssClass="tw-h-32 tw-mb-4" />
                <uv:overtime-left hours="${overtimeLeft}" cssClass="tw-h-32 tw-mb-4" />
            </div>
        </div>
        <%-- End of row --%>
    </div>
    <%-- End of container --%>
</div>
<%-- End of content --%>

</body>
</html>
