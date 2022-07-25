<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<button
    class="tw-bg-transparent icon-link tw-px-1 tw-py-0 tw-hidden sm:tw-flex print:tw-hidden"
    data-title="<spring:message code='action.print' />"
    onclick="window.print(); return false;"
>
    <icon:printer className="tw-w-5 tw-h-5" />
</button>
