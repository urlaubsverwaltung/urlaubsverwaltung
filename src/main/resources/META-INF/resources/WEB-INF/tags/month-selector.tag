<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="month" type="java.lang.String" required="true" %>

<div id="month-selection" class="legend-dropdown dropdown">
    <a id="monthDropdownLabel" data-target="#" href="#" data-toggle="dropdown"
       aria-haspopup="true" role="button" aria-expanded="false">
        <span class='labelText'><c:out value="${month}" /></span><span class="caret"></span>
    </a>

    <ul class="dropdown-menu" role="menu" aria-labelledby="monthDropdownLabel">
    <c:forEach begin="1" end="12" var="month">
        <li><a href="#" data-month='${month}'><spring:message code="month.${month}" /></a><li>
    </c:forEach>
    </ul>
</div>