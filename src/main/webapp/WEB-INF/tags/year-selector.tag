<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="year" type="java.lang.String" required="true" %>
<%@attribute name="hrefPrefix" type="java.lang.String" required="true" %>

<jsp:useBean id="date" class="java.util.Date" />

<span id="year-selection" class="tw-leading-6 dropdown legend-dropdown">
    <a id="dropdownLabel" href="#" data-toggle="dropdown" aria-haspopup="true" role="button" aria-expanded="false" class="">
        <c:out value="${year}" /><span class="caret"></span>
    </a>
    <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownLabel">
        <c:forEach begin="0" end="10" varStatus="loop">
            <c:set var="y" value="${date.year + 1900 + 2 - loop.count}" />
            <li><a href="${hrefPrefix.concat(y)}"><c:out value="${y}" /></a></li>
        </c:forEach>
    </ul>
</span>
