<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<spring:url var="URL_PREFIX" value="/web"/>

<!DOCTYPE html>
<html lang="${language}">
<head>
    <title>
        <spring:message code="calendar.share.header.title" />
    </title>
    <uv:custom-head />
</head>
<body>

<uv:menu />

<div class="content container">
    <header>
        <h1 class="mb-8 text-2xl text-medium">
            <spring:message code="calendar.share.title" />
        </h1>
    </header>
    <main>
        <form:form method="POST" action="${URL_PREFIX}/persons/${privateCalendarShare.personId}/calendar/share/me" modelAttribute="privateCalendarShare">
            <fieldset class="mb-4">
                <legend class="text-xl">
                    <spring:message code="calendar.share.me.title" />
                </legend>
                <c:choose>
                <c:when test="${empty privateCalendarShare.calendarUrl}">
                <div class="max-w-3xl">
                    <p class="mb-8 text-base">
                        <spring:message code="calendar.share.me.paragraph.status" />
                    </p>
                    <p class="mb-2 text-base">
                        <spring:message code="calendar.share.me.paragraph.info" />
                    </p>
                    <p class="mb-4 text-base">
                        <spring:message code="calendar.share.me.paragraph.info.reset" />
                    </p>
                    <button type="submit" class="btn btn-primary">
                        <spring:message code="calendar.share.me.form.submit.text" />
                    </button>
                </div>
                </c:when>
                <c:otherwise>
                <div class="max-w-3xl">
                    <p class="mb-8 text-base">
                        <spring:message code="calendar.share.me.isshared.paragraph.status" />
                    </p>
                    <p class="mb-2 text-base">
                        <spring:message code="calendar.share.me.isshared.paragraph.info" />
                    </p>
                    <input type="text" value="${privateCalendarShare.calendarUrl}" class="mb-8 px-3 py-2 text-base w-full" readonly />
                    <p class="mb-8 text-base">
                        <spring:message code="calendar.share.me.reset.paragraph" />
                    </p>
                    <div class="flex flex-col sm:flex-row">
                        <button type="submit" class="btn btn-default mb-4 sm:mb-0">
                            <spring:message code="calendar.share.me.reset.form.submit.text" />
                        </button>
                        <button type="submit" name="unlink" class="btn btn-default sm:mb-0">
                            <spring:message code="calendar.share.me.unlink.form.submit.text" />
                        </button>
                    </div>
                </div>
                </c:otherwise>
                </c:choose>
            </fieldset>
        </form:form>
    </main>
</div>

</body>
</html>
