<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<div class="tw-max-w-3xl tw-bg-yellow-100 tw-border tw-border-yellow-300 tw-p-4 tw-flex tw-flex-col sm:tw-flex-row">
    <div class="tw-mb-4 sm:tw-mb-0 sm:tw-mr-4 lg:tw-mr-14 tw-flex tw-items-center sm:tw-items-start">
        <icon:information-circle
            className="tw-text-yellow-400 tw-w-6 tw-h-6 tw-mr-2"
            solid="true"
        />
        <span class="tw-text-black tw-text-opacity-75 tw-font-bold tw-text-sm sm:tw-mt-px">
            <spring:message code="privacy-box.title" />
        </span>
    </div>
    <div class="tw-flex-1 tw-text-sm lg:tw-text-base tw-text-black tw-text-opacity-80">
        <jsp:doBody />
    </div>
</div>
