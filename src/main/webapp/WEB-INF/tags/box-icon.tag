<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@attribute name="className" type="java.lang.String" required="false" %>

<div class="tw-rounded-full tw-flex tw-items-center tw-justify-center tw-w-16 tw-h-16 ${className}">
    <jsp:doBody />
</div>
