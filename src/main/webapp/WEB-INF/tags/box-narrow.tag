<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="icon" fragment="true" required="true" %>
<%@attribute name="className" type="java.lang.String" %>

<uv:box__ className="tw-p-0 ${className}">
    <jsp:attribute name="icon">
        <jsp:invoke fragment="icon" />
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody />
    </jsp:body>
</uv:box__>
