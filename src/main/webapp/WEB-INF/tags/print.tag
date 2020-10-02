<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<a
    href="#"
    class="icon-link tw-px-1 tw-hidden sm:tw-flex print:tw-hidden"
    data-title="<spring:message code='action.print' />"
    onclick="window.print(); return false;"
>
    <uv:icon-printer className="tw-w-5 tw-h-5" />
</a>
