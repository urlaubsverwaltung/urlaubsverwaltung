<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="icon" fragment="true" required="true" %>
<%@attribute name="className" type="java.lang.String" required="false" %>

<div class="box tw-flex tw-items-start ${className}">
    <div class="tw-mr-6 tw-p-1">
        <jsp:invoke fragment="icon" />
    </div>
    <div class="box-text tw-flex-1 tw-flex tw-flex-col">
        <jsp:doBody />
    </div>
</div>
