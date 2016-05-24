<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="month" type="java.lang.String" required="true" %>

<div id="month-selection" class="legend-dropdown dropdown">
    <a id="monthDropdownLabel" data-target="#" href="#" data-toggle="dropdown"
       aria-haspopup="true" role="button" aria-expanded="false">
        <span class='labelText'><c:out value="${month}" /></span><span class="caret"></span>
    </a>

    <ul class="dropdown-menu" role="menu" aria-labelledby="monthDropdownLabel">
        <li><a href="#" data-month='1'>Januar</a><li>
        <li><a href="#" data-month='2'>Februar</a><li>
        <li><a href="#" data-month='3'>M&auml;rz</a><li>
        <li><a href="#" data-month='4'>April</a><li>
        <li><a href="#" data-month='5'>Mai</a><li>
        <li><a href="#" data-month='6'>Juni</a><li>
        <li><a href="#" data-month='7'>Juli</a><li>
        <li><a href="#" data-month='8'>August</a><li>
        <li><a href="#" data-month='9'>September</a><li>
        <li><a href="#" data-month='10'>Oktober</a><li>
        <li><a href="#" data-month='11'>November</a><li>
        <li><a href="#" data-month='12'>Dezember</a><li>
    </ul>
</div>