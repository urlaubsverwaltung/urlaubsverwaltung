<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<%@attribute name="value" type="java.lang.String" required="true" %>
<%@attribute name="selected" type="java.lang.Boolean" %>

<option value="${value}" ${selected ? 'selected="selected"' : ''}>
    <jsp:doBody />
</option>
