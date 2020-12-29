<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="addon" fragment="true" required="true" %>
<%@attribute name="hasError" type="java.lang.Boolean" required="false" %>

<span class="input-group ${hasError ? 'has-error' : ''}">
    <jsp:doBody />
    <span class="input-group-addon">
        <jsp:invoke fragment="addon" />
    </span>
</span>
